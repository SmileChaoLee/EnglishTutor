package com.smile.englishtutor.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.smile.englishtutor.mvi.BaseUiState
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.utilities.VoiceToTextManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<S : BaseUiState, I : BaseUserIntent>(
    application: Application,
    initialState: S
) : AndroidViewModel(application) {

    protected val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    protected abstract val TAG: String

    protected val voiceToTextManager = VoiceToTextManager(
        context = application,
        onResult = { text ->
            onVoiceResult(text)
        },
        onError = { error ->
            LogUtil.e(TAG, "voiceToTextManager.error = $error")
            updateState { copyWithError(it, "Voice Error: $error") }
        },
        onListeningStatusChange = { isListening ->
            updateState { copyWithListeningStatus(it, isListening) }
        }
    )

    abstract fun handleIntent(intent: I)

    protected abstract fun onVoiceResult(text: String)

    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected abstract fun copyWithError(state: S, error: String?): S
    protected abstract fun copyWithListeningStatus(state: S, isListening: Boolean): S
    protected abstract fun copyWithInputText(state: S, text: String): S
    protected abstract fun copyWithLoadingStatus(state: S, isLoading: Boolean): S
    protected abstract fun copyWithPermissionStatus(state: S, hasPermission: Boolean): S

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
    }
}
