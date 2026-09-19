package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.YouTubeVideo

data class StoryUiState (
    val videos: List<YouTubeVideo> = emptyList(),
    override val inputText: String = "",
    override val isLoading: Boolean = false,
    override val isListening: Boolean = false,
    override val hasRecordAudioPermission: Boolean = false,
    override val error: String? = null
) : BaseUiState(inputText, isLoading, isListening, hasRecordAudioPermission, error)