package com.smile.englishtutor.mvi

sealed class BaseUserIntent {
    data class UpdateInput(val text: String) : BaseUserIntent()
    object ToggleVoiceInput : BaseUserIntent()
    data class UpdatePermissionStatus(val hasPermission: Boolean) : BaseUserIntent()
    object ClearError : BaseUserIntent()
}
