package com.smile.englishtutor.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.smile.englishtutor.mvi.BaseCommonState
import com.smile.englishtutor.mvi.BaseUiState
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.utilities.TextToSpeechManager
import com.smile.englishtutor.utilities.VoiceToTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<S : BaseUiState<S>, I : BaseUserIntent>(
    application: Application,
    initialState: S
) : AndroidViewModel(application) {

    protected val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    protected abstract val TAG: String

    protected open fun onSpeechStart(id: String) {}
    protected open fun onSpeechDone(id: String) {}
    protected open fun onSpeechError(id: String) {}

    protected val ttsManager = TextToSpeechManager(
        context = application,
        onSpeechStart = { id -> onSpeechStart(id) },
        onSpeechDone = { id -> onSpeechDone(id) },
        onSpeechError = { id -> onSpeechError(id) }
    )

    protected val voiceToTextManager = VoiceToTextManager(
        context = application,
        onResult = { text ->
            handleBaseIntent(BaseUserIntent.UpdateInput(text))
        },
        onError = { error ->
            LogUtil.e(TAG, "voiceToTextManager.error = $error")
            handleBaseIntent(BaseUserIntent.ClearError) // Example use, or keep old
            updateState { copyWithError(it, "Voice Error: $error") }
        },
        onListeningStatusChange = { isListening ->
            updateState { copyWithListeningStatus(it, isListening) }
        }
    )

    abstract fun handleIntent(intent: BaseUserIntent)

    protected fun handleBaseIntent(intent: BaseUserIntent): Boolean {
        return when (intent) {
            is BaseUserIntent.UpdateInput -> {
                updateState { copyWithInputText(it, intent.text) }
                true
            }
            BaseUserIntent.ToggleVoiceInput -> {
                toggleVoiceInput()
                true
            }
            is BaseUserIntent.UpdatePermissionStatus -> {
                updateState { copyWithPermissionStatus(it, intent.hasPermission) }
                true
            }
            BaseUserIntent.ClearError -> {
                updateState { copyWithError(it, null) }
                true
            }
            else -> false
        }
    }

    protected abstract fun onVoiceResult(text: String)

    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun copyWithError(state: S, error: String?): S =
        state.updateBase(state.base.copy(error = error))

    protected fun copyWithListeningStatus(state: S, isListening: Boolean): S =
        state.updateBase(state.base.copy(isListening = isListening))

    protected fun copyWithInputText(state: S, text: String): S =
        state.updateBase(state.base.copy(inputText = text))

    protected fun copyWithLoadingStatus(state: S, isLoading: Boolean): S =
        state.updateBase(state.base.copy(isLoading = isLoading))

    protected fun copyWithPermissionStatus(state: S, hasPermission: Boolean): S =
        state.updateBase(state.base.copy(hasRecordAudioPermission = hasPermission))

    protected fun toggleVoiceInput() {
        val isListening = _state.value.isListening
        LogUtil.d(TAG, "ToggleVoiceInput. isListening = $isListening")
        if (isListening) {
            voiceToTextManager.stopListening()
        } else {
            voiceToTextManager.startListening()
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        voiceToTextManager.destroy()
        ttsManager.destroy()
    }
}
