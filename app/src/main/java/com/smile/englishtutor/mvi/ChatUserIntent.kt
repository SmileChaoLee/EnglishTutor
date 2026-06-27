package com.smile.englishtutor.mvi

sealed class ChatUserIntent {
    data class UpdateInput(val text: String) : ChatUserIntent()
    object SendMessage : ChatUserIntent()
    object ToggleVoiceInput : ChatUserIntent()
    data class UpdatePermissionStatus(val hasPermission: Boolean) : ChatUserIntent()
    object ClearError : ChatUserIntent()
    data class SpeakText(val messageId: String, val text: String) : ChatUserIntent()
}
