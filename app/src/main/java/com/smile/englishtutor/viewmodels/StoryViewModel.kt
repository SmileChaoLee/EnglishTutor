package com.smile.englishtutor.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.smile.englishtutor.mvi.StoryUiState
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.utilities.LogUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "StoryViewModel"
    }

    private val _state = MutableStateFlow(StoryUiState())
    val state: StateFlow<StoryUiState> = _state.asStateFlow()

    fun handleIntent(intent: StoryUserIntent) {
        when (intent) {
            is StoryUserIntent.UpdateInput -> {
                _state.update { it.copy(inputText = intent.text) }
            }
            is StoryUserIntent.SendMessage -> {
                // sendMessage(_state.value.inputText)
            }
            is StoryUserIntent.ToggleVoiceInput -> {
                LogUtil.d(TAG, "ToggleVoiceInput. isListening = ${_state.value.isListening}")
                /*
                if (_state.value.isListening) {
                    voiceToTextManager.stopListening()
                } else {
                    voiceToTextManager.startListening()
                }
                */
            }
            is StoryUserIntent.UpdatePermissionStatus -> {
                LogUtil.d(TAG, "handleIntent.StoryUserIntent.UpdatePermissionStatus")
                _state.update { it.copy(hasRecordAudioPermission = intent.hasPermission) }
            }
            is StoryUserIntent.ClearError -> {
                LogUtil.d(TAG, "handleIntent.StoryUserIntent.ClearError")
                _state.update { it.copy(error = null) }
            }
        }
    }
}