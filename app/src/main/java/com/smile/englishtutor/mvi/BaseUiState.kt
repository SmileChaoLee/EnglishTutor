package com.smile.englishtutor.mvi

data class BaseCommonState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val hasRecordAudioPermission: Boolean = false,
    val error: String? = null
)

abstract class BaseUiState<S>(open val base: BaseCommonState) {
    val inputText get() = base.inputText
    val isLoading get() = base.isLoading
    val isListening get() = base.isListening
    val hasRecordAudioPermission get() = base.hasRecordAudioPermission
    val error get() = base.error

    abstract fun updateBase(newBase: BaseCommonState): S
}
