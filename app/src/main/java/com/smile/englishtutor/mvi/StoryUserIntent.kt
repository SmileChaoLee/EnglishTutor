package com.smile.englishtutor.mvi

sealed class StoryUserIntent : BaseUserIntent {
    data class UpdateInput(val text: String) : StoryUserIntent()
    object SendMessage : StoryUserIntent()
    object ToggleVoiceInput : StoryUserIntent()
    data class UpdatePermissionStatus(val hasPermission: Boolean) : StoryUserIntent()
    object ClearError : StoryUserIntent()
}