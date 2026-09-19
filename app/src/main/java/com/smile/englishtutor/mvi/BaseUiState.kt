package com.smile.englishtutor.mvi

abstract class BaseUiState(
    open val inputText: String = "",
    open val isLoading: Boolean = false,
    open val isListening: Boolean = false,
    open val hasRecordAudioPermission: Boolean = false,
    open val error: String? = null
)
