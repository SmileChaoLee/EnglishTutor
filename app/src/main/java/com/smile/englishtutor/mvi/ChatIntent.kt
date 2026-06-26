package com.smile.englishtutor.mvi

sealed class ChatIntent {
    data class UpdateInput(val text: String) : ChatIntent()
    object SendMessage : ChatIntent()
}
