package com.smile.englishtutor.mvi

import com.smile.englishtutor.models.YouTubeVideo

data class StoryUiState (
    val videos: List<YouTubeVideo> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val hasRecordAudioPermission: Boolean = false,
    val error: String? = null
)