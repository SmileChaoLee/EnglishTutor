package com.smile.englishtutor.mvi

sealed class StoryUserIntent : BaseUserIntent() {
    object GetStories : StoryUserIntent()
    data class SpeakText(val text: String) : StoryUserIntent()
    data class PlayVideo(val videoId: String?) : StoryUserIntent()
    data class UpdateFullScreen(val isFullScreen: Boolean) : StoryUserIntent()
}
