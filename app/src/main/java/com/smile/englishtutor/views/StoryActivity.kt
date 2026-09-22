package com.smile.englishtutor.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.ui.StoryScreen
import com.smile.englishtutor.ui.MyTopAppBar
import com.smile.englishtutor.viewmodels.StoryViewModel
import com.smile.englishtutor.R
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing

class StoryActivity : BaseActivity() {

    companion object {
        const val TAG = "StoryActivity"
    }

    override fun initViewModel() {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StoryViewModel(application) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[StoryViewModel::class.java]
        viewModel.handleIntent(BaseUserIntent.UpdatePermissionStatus(hasRecordAudioPermission))
    }

    @Composable
    override fun CreateMainUI(modifier: Modifier) {
        val storyViewModel = viewModel as StoryViewModel
        val state by storyViewModel.state.collectAsState()
        val text = getString(R.string.whatStoriesAreYouLookingFor)
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                IconButton(onClick = {
                    storyViewModel.handleIntent(StoryUserIntent.SpeakText(text))
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak Out",
                        tint = if (state.isSpeaking) Color.Red else Color.Black
                    )
                }
            }
            StoryScreen(
                modifier = Modifier.weight(1f),
                viewModel = storyViewModel
            )
        }
        LaunchedEffect(Unit) {
            storyViewModel.handleIntent(StoryUserIntent.SpeakText(text))
        }
    }

    @Composable
    override fun ActivityScaffold(title: String, content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit) {
        val storyViewModel = viewModel as StoryViewModel
        val state by storyViewModel.state.collectAsState()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!state.isFullScreen) {
                    MyTopAppBar(title)
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            content = content
        )
    }
}
