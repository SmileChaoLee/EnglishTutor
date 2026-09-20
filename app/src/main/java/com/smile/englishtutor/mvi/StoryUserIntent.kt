package com.smile.englishtutor.mvi

sealed class StoryUserIntent : BaseUserIntent() {
    object GetStories : StoryUserIntent()
}
