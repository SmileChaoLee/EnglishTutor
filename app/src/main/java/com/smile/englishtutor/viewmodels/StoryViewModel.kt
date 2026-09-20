package com.smile.englishtutor.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.smile.englishtutor.mvi.StoryUiState
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.retrofit.U2bRestApiSync
import com.smile.englishtutor.utilities.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoryViewModel(application: Application) : BaseViewModel<StoryUiState, StoryUserIntent>(application, StoryUiState()) {
    override val TAG = "StoryViewModel"

    override fun onVoiceResult(text: String) {
        handleIntent(StoryUserIntent.UpdateInput(text))
    }

    override fun handleIntent(intent: StoryUserIntent) {
        when (intent) {
            is StoryUserIntent.UpdateInput -> {
                updateState { copyWithInputText(it, intent.text) }
            }
            is StoryUserIntent.SendMessage -> {
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
            is StoryUserIntent.ToggleVoiceInput -> {
                toggleVoiceInput()
            }
            is StoryUserIntent.UpdatePermissionStatus -> {
                LogUtil.d(TAG, "handleIntent.StoryUserIntent.UpdatePermissionStatus")
                updateState { copyWithPermissionStatus(it, intent.hasPermission) }
            }
            is StoryUserIntent.ClearError -> {
                LogUtil.d(TAG, "handleIntent.StoryUserIntent.ClearError")
                updateState { copyWithError(it, null) }
            }
        }
    }
}