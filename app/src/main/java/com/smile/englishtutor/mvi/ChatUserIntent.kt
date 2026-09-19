package com.smile.englishtutor.mvi

sealed class ChatUserIntent : BaseUserIntent {
    data class UpdateInput(val text: String) : ChatUserIntent()
    object SendMessage : ChatUserIntent()
    object ToggleVoiceInput : ChatUserIntent()
    data class UpdatePermissionStatus(val hasPermission: Boolean) : ChatUserIntent()
    object ClearError : ChatUserIntent()
    data class SpeakText(val messageId: String, val text: String) : ChatUserIntent()
    data class Translate(val translateFrom: String, val translateTo: String) : ChatUserIntent()
}
