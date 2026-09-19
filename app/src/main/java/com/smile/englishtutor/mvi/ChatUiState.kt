package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    override val inputText: String = "",
    override val isLoading: Boolean = false,
    override val isListening: Boolean = false,
    override val hasRecordAudioPermission: Boolean = false,
    override val error: String? = null,
    val speakingMessageId: String? = null
) : BaseUiState(inputText, isLoading, isListening, hasRecordAudioPermission, error)
