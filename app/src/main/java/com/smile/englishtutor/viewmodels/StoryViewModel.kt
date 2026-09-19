package com.smile.englishtutor.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.mvi.StoryUiState
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.retrofit.U2bRestApiSync
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.utilities.VoiceToTextManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "StoryViewModel"
    }

    private val _state = MutableStateFlow(StoryUiState())
    val state: StateFlow<StoryUiState> = _state.asStateFlow()
    private val voiceToTextManager = VoiceToTextManager(
        context = application,
        onResult = { text ->
            handleIntent(StoryUserIntent.UpdateInput(text))
        },
        onError = { error ->
            LogUtil.e(TAG, "voiceToTextManager.error = $error")
            _state.update { it.copy(error = "Voice Error: $error") }
        },
        onListeningStatusChange = { isListening ->
            _state.update { it.copy(isListening = isListening) }
        }
    )

    fun handleIntent(intent: StoryUserIntent) {
        when (intent) {
            is StoryUserIntent.UpdateInput -> {
                _state.update { it.copy(inputText = intent.text) }
            }
            is StoryUserIntent.SendMessage -> {
                _state.update { it.copy(isLoading = true) }
                viewModelScope.launch(Dispatchers.IO) {
                    val searchTerm = "Stories about ${_state.value.inputText}"
                    val ytVideos = U2bRestApiSync.getVideos(searchTerm)
                    LogUtil.d(TAG, "SendMessage.ytVideos.size = ${ytVideos.size}")
                    _state.update {
                        it.copy(
                            videos = ytVideos,
                            inputText = "",
                            isLoading = false
                        )
                    }
                }
            }
            is StoryUserIntent.ToggleVoiceInput -> {
                LogUtil.d(TAG, "ToggleVoiceInput. isListening = ${_state.value.isListening}")
                if (_state.value.isListening) {
                    voiceToTextManager.stopListening()
                } else {
                    voiceToTextManager.startListening()
                }
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