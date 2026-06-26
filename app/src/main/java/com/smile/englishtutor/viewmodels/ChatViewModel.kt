package com.smile.englishtutor.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.models.ChatMessage
import com.smile.englishtutor.mvi.ChatIntent
import com.smile.englishtutor.mvi.ChatState
import com.smile.englishtutor.retrofit.RestApiSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        sendInitialMessage()
    }

    fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.UpdateInput -> {
                _state.update { it.copy(inputText = intent.text) }
            }
            ChatIntent.SendMessage -> {
                sendMessage(_state.value.inputText)
            }
        }
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
                it.copy(
                    messages = it.messages + ChatMessage(
                        text = response?.agentResponse ?: "Error: No response from agent",
                        isUser = false
                    ),
                    isLoading = false
                )
            }
        }
    }
}
