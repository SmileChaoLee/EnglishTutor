package com.smile.englishtutor.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.models.ChatMessage
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.mvi.ChatUiState
import com.smile.englishtutor.retrofit.RestApiSync
import com.smile.englishtutor.utilities.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    application: Application,
    private val option: Int = Constants.CONVERSATION_OPTION
) : BaseViewModel<ChatUiState, ChatUserIntent>(application, ChatUiState()) {

    override val TAG = "ChatViewModel"

    override fun onVoiceResult(text: String) {
        handleBaseIntent(BaseUserIntent.UpdateInput(text))
    }

    override fun onSpeechStart(id: String) {
        updateState { it.copy(speakingMessageId = id) }
    }

    override fun onSpeechDone(id: String) {
        updateState { it.copy(speakingMessageId = null) }
    }

    override fun onSpeechError(id: String) {
        updateState { it.copy(speakingMessageId = null) }
    }

    private var isInitial = true
    private var translateFrom: String? = null
    private var translateTo: String? = null
    private val historyMessages = ArrayList<Map<String, String>>()
    private val maxHistorySize = 20

    init {
        isInitial = true
        translateFrom = Constants.ENGLISH_LANGUAGE
        translateTo = Constants.ENGLISH_LANGUAGE
        sendInitialMessage()
    }

    override fun handleIntent(intent: BaseUserIntent) {
        if (handleBaseIntent(intent)) return
        
        when (intent) {
            ChatUserIntent.SendMessage -> {
                sendMessage(_state.value.inputText)
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
            else -> {}
        }
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
            }
            updateState {
                val agentMsg = response?.agentResponse ?: "Error: No response from agent"
                val hSize = historyMessages.size
                if (hSize >= maxHistorySize) {
                    // because maxHistorySize >= 2, so we are able to do the following
                    historyMessages.removeAt(1)
                    historyMessages.removeAt(0)
                }
                historyMessages.add(mapOf("role" to "user", "content" to requestText))
                historyMessages.add(mapOf("role" to "assistant", "content" to agentMsg))
                LogUtil.d(TAG,"historyMessages.size = ${historyMessages.size}")
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
