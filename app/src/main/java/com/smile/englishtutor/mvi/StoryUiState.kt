package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.YouTubeVideo

data class StoryUiState (
    override var base: BaseCommonState = BaseCommonState(),
    val videos: List<YouTubeVideo> = emptyList()
) : BaseUiState<StoryUiState>(base) {
    override fun updateBase(newBase: BaseCommonState) = copy(base = newBase)
}
