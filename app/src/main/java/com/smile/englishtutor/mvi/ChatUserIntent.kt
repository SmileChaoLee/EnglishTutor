package com.smile.englishtutor.mvi

sealed class ChatUserIntent : BaseUserIntent() {
    object SendMessage : ChatUserIntent()
    data class SpeakText(val messageId: String, val text: String) : ChatUserIntent()
    data class Translate(val translateFrom: String, val translateTo: String) : ChatUserIntent()
}
