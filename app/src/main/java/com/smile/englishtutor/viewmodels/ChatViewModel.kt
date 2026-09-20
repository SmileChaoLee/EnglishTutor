package com.smile.englishtutor.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.models.ChatMessage
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.mvi.ChatUiState
import com.smile.englishtutor.retrofit.RestApiSync
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.utilities.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    application: Application,
    private val option: Int = Constants.CONVERSATION_OPTION
) : BaseViewModel<ChatUiState, ChatUserIntent>(application, ChatUiState()) {

    override val TAG = "ChatViewModel"

    override fun onVoiceResult(text: String) {
        handleIntent(ChatUserIntent.UpdateInput(text))
    }

    private val ttsManager = TextToSpeechManager(
        context = application,
        onSpeechStart = { id ->
            updateState { it.copy(speakingMessageId = id) }
        },
        onSpeechDone = { _ ->
            updateState { it.copy(speakingMessageId = null) }
        },
        onSpeechError = { _ ->
            updateState { it.copy(speakingMessageId = null) }
        }
    )

    private var isInitial = true
    private var translateFrom: String? = null
    private var translateTo: String? = null
    private val historyMessages = ArrayList<Map<String, String>>()
    private val maxHistorySize = 10

    init {
        isInitial = true
        translateFrom = Constants.ENGLISH_LANGUAGE
        translateTo = Constants.ENGLISH_LANGUAGE
        sendInitialMessage()
    }

    override fun handleIntent(intent: ChatUserIntent) {
        when (intent) {
            is ChatUserIntent.UpdateInput -> {
                updateState { copyWithInputText(it, intent.text) }
            }
            ChatUserIntent.SendMessage -> {
                sendMessage(_state.value.inputText)
            }
            ChatUserIntent.ToggleVoiceInput -> {
                toggleVoiceInput()
            }
            is ChatUserIntent.UpdatePermissionStatus -> {
                updateState { copyWithPermissionStatus(it, intent.hasPermission) }
            }
            ChatUserIntent.ClearError -> {
                updateState { copyWithError(it, null) }
            }
            is ChatUserIntent.SpeakText -> {
                ttsManager.speak(intent.text, intent.messageId)
            }
            is ChatUserIntent.Translate -> {
                translateFrom = intent.translateFrom
                translateTo = intent.translateTo
                LogUtil.d(TAG, "handleIntent.translateFrom = $translateFrom")
                LogUtil.d(TAG, "handleIntent.translateTo = $translateTo")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.destroy()
    }

    private fun sendInitialMessage() {
        sendMessage("Who are you?")
        isInitial = false
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        if (!isInitial) {
            val userMessage = ChatMessage(text = text, isUser = true)
            updateState {
                it.copy(
                    messages = it.messages + userMessage,
                    base = it.base.copy(inputText = "", isLoading = true)
                )
            }
        } else {
            updateState { copyWithLoadingStatus(it, true) }
        }

        viewModelScope.launch {
            var requestText = text
            if (option == Constants.TRANSLATION_OPTION && !isInitial) {
                // translation
                requestText  = "Translate $text from $translateFrom to $translateTo"
            }
            val response = withContext(Dispatchers.IO) {
                RestApiSync.getAgentResponse(requestText, option, historyMessages)
                // RestApiSync.getAgentResponse(requestText, option)
            }
            updateState {
                val agentMsg = response?.agentResponse ?: "Error: No response from agent"
                if (historyMessages.size >= maxHistorySize) {
                    historyMessages.removeAt(0)
                }
                historyMessages.add(mapOf("role" to "user", "content" to requestText))
                historyMessages.add(mapOf("role" to "assistant", "content" to agentMsg))

                val agentMessage = ChatMessage(text = agentMsg, isUser = false)
                ttsManager.speak(agentMsg, agentMessage.id)
                it.copy(
                    messages = it.messages + agentMessage,
                    base = it.base.copy(isLoading = false)
                )
            }
        }
    }
}
