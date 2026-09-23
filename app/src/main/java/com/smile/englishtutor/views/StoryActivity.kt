package com.smile.englishtutor.views

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smile.englishtutor.EnglishTutorApp
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.ui.ShowAdmobBanner
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
    override fun BottomBannerArea() {
        val storyViewModel = viewModel as StoryViewModel
        val state by storyViewModel.state.collectAsState()
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val context = LocalContext.current
        val activity = context as? Activity
        val isPhone = activity?.let { com.smile.smilelibraries.utilities.ScreenUtil.getDeviceType(it) == com.smile.smilelibraries.utilities.ScreenUtil.DEVICE_TYPE_PHONE } ?: false
        val backButtonText = getString(R.string.backStr)

        val onBackToVideoList = {
            if (isPhone) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            storyViewModel.handleIntent(StoryUserIntent.CloseVideo)
        }

        if (state.selectedVideoId != null && (!state.isFullScreen || isPhone)) {
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToVideoList) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Video List",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = backButtonText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onBackToVideoList() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ShowAdmobBanner(
                        modifier = Modifier.weight(1f),
                        bannerID = EnglishTutorApp.ADMOB_BANNER_ID
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackToVideoList) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Video List",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = backButtonText,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onBackToVideoList() }
                        )
                    }
                    ShowAdmobBanner(
                        modifier = Modifier.fillMaxWidth(),
                        bannerID = EnglishTutorApp.ADMOB_BANNER_ID
                    )
                }
            }
        } else {
            super.BottomBannerArea()
        }
    }

    @Composable
    override fun CreateMainUI(modifier: Modifier) {
        val storyViewModel = viewModel as StoryViewModel
        val state by storyViewModel.state.collectAsState()
        val text = getString(R.string.whatStoriesAreYouLookingFor)
        Column(modifier = modifier) {
            if (state.selectedVideoId == null) {
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
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val showTopBar = !state.isFullScreen && !(state.selectedVideoId != null && isLandscape)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showTopBar) {
                    MyTopAppBar(
                        title = title,
                        onBackClick = if (state.selectedVideoId != null) {
                            { storyViewModel.handleIntent(StoryUserIntent.CloseVideo) }
                        } else null
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            content = content
        )
    }
}
