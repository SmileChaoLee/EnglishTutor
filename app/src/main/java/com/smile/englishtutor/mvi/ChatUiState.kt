package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.ChatMessage

data class ChatUiState(
    override var base: BaseCommonState = BaseCommonState(),
    val messages: List<ChatMessage> = emptyList(),
    val speakingMessageId: String? = null
) : BaseUiState<ChatUiState>(base) {
    override fun updateBase(newBase: BaseCommonState) = copy(base = newBase)
}
