package com.smile.englishtutor.mvi

sealed class StoryUserIntent : BaseUserIntent() {
    object GetStories : StoryUserIntent()
    data class SpeakText(val text: String) : StoryUserIntent()
}
