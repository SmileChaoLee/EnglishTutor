package com.smile.englishtutor.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.models.ChatMessage
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.mvi.ChatUiState
import com.smile.englishtutor.retrofit.RestApiSync
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.utilities.TextToSpeechManager
import com.smile.englishtutor.utilities.VoiceToTextManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val voiceToTextManager = VoiceToTextManager(
        context = application,
        onResult = { text ->
            handleIntent(ChatUserIntent.UpdateInput(text))
        },
        onError = { error ->
            LogUtil.e(TAG, "voiceToTextManager.error = $error")
            _state.update { it.copy(error = "Voice Error: $error") }
        },
        onListeningStatusChange = { isListening ->
            _state.update { it.copy(isListening = isListening) }
        }
    )

    private val ttsManager = TextToSpeechManager(
        context = application,
        onSpeechStart = { id ->
            _state.update { it.copy(speakingMessageId = id) }
        },
        onSpeechDone = { _ ->
            _state.update { it.copy(speakingMessageId = null) }
        },
        onSpeechError = { _ ->
            _state.update { it.copy(speakingMessageId = null) }
        }
    )

    init {
        sendInitialMessage()
    }

    fun handleIntent(intent: ChatUserIntent) {
        when (intent) {
            is ChatUserIntent.UpdateInput -> {
                _state.update { it.copy(inputText = intent.text) }
            }
            ChatUserIntent.SendMessage -> {
                sendMessage(_state.value.inputText)
            }
            ChatUserIntent.ToggleVoiceInput -> {
                LogUtil.d(TAG, "ToggleVoiceInput. isListening = ${_state.value.isListening}")
                if (_state.value.isListening) {
                    voiceToTextManager.stopListening()
                } else {
                    voiceToTextManager.startListening()
                }
            }
            is ChatUserIntent.UpdatePermissionStatus -> {
                _state.update { it.copy(hasRecordAudioPermission = intent.hasPermission) }
            }
            ChatUserIntent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            is ChatUserIntent.SpeakText -> {
                ttsManager.speak(intent.text, intent.messageId)
            }
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        voiceToTextManager.destroy()
        ttsManager.destroy()
    }

    private fun sendInitialMessage() {
        sendMessage("Who are you?", isInitial = true)
    }

    private fun sendMessage(text: String, isInitial: Boolean = false) {
        if (text.isBlank()) return

        if (!isInitial) {
            val userMessage = ChatMessage(text = text, isUser = true)
            _state.update {
                it.copy(
                    messages = it.messages + userMessage,
                    inputText = "",
                    isLoading = true
                )
            }
        } else {
            _state.update { it.copy(isLoading = true) }
        }

        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                RestApiSync.getAgentResponse(text)
            }
            
            _state.update {
                val agentMsg = response?.agentResponse ?: "Error: No response from agent"
                val agentMessage = ChatMessage(text = agentMsg, isUser = false)
                ttsManager.speak(agentMsg, agentMessage.id)
                it.copy(
                    messages = it.messages + agentMessage,
                    isLoading = false
                )
            }
        }
    }
}
