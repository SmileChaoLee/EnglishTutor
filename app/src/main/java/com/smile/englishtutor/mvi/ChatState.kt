package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.ChatMessage

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)
