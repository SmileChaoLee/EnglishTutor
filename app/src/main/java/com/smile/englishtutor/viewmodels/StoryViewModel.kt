package com.smile.englishtutor.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.StoryUiState
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.retrofit.U2bRestApiSync
import com.smile.englishtutor.utilities.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoryViewModel(
    application: Application
) : BaseViewModel<StoryUiState, StoryUserIntent>(application, StoryUiState()) {
    override val TAG = "StoryViewModel"

    override fun onVoiceResult(text: String) {
        handleBaseIntent(BaseUserIntent.UpdateInput(text))
    }

    override fun onSpeechStart(id: String) {
        updateState { it.copy(isSpeaking = true) }
    }

    override fun onSpeechDone(id: String) {
        updateState { it.copy(isSpeaking = false) }
    }

    override fun onSpeechError(id: String) {
        updateState { it.copy(isSpeaking = false) }
    }

    override fun handleIntent(intent: BaseUserIntent) {
        if (handleBaseIntent(intent)) return
        
        when (intent) {
            is StoryUserIntent.GetStories -> {
                updateState { copyWithLoadingStatus(it, true) }
                viewModelScope.launch(Dispatchers.IO) {
                    val searchTerm = "Stories about ${_state.value.inputText}"
                    val ytVideos = U2bRestApiSync.getVideos(searchTerm)
                    LogUtil.d(TAG, "SendMessage.ytVideos.size = ${ytVideos.size}")
                    updateState {
                        it.copy(
                            videos = ytVideos,
                            base = it.base.copy(inputText = "", isLoading = false)
                        )
                    }
                }
            }
            is StoryUserIntent.SpeakText -> {
                updateState { it.copy(isSpeaking = true) }
                ttsManager.speak(intent.text, "story_id")
            }
            is StoryUserIntent.PlayVideo -> {
                updateState { it.copy(selectedVideoId = intent.videoId) }
            }
            is StoryUserIntent.UpdateFullScreen -> {
                updateState { it.copy(isFullScreen = intent.isFullScreen) }
            }
            else -> {}
        }
    }
}
