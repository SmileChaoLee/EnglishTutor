package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val hasRecordAudioPermission: Boolean = false,
    val error: String? = null,
    val speakingMessageId: String? = null
)
