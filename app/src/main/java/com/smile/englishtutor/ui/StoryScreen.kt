package com.smile.englishtutor.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smile.englishtutor.R
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.StoryViewModel

private const val TAG = "StoryScreen"
private const val SPINNER_SCALE = 0.15f
private val SPINNER_MIN_SIZE = 48.dp
private val SPINNER_MAX_SIZE = 120.dp
private val VIDEO_THUMBNAIL_MIN_HEIGHT = 120.dp
private val VIDEO_THUMBNAIL_MAX_HEIGHT = 400.dp

@Composable
fun StoryScreen(
    modifier: Modifier = Modifier,
    viewModel: StoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val gridState = rememberLazyGridState()
    val context = LocalContext.current

    LaunchedEffect(state.videos.size) {
        LogUtil.d(TAG, "LaunchedEffect.state.videos.size")
        if (state.videos.isNotEmpty()) {
            gridState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(state.error) {
        LogUtil.d(TAG, "LaunchedEffect.state.error")
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.handleIntent(BaseUserIntent.ClearError)
        }
    }

    BackHandler(enabled = state.selectedVideoId != null) {
        val currentActivity = context as? Activity
        val deviceType = currentActivity?.let { com.smile.smilelibraries.utilities.ScreenUtil.getDeviceType(it) }
        if (deviceType == com.smile.smilelibraries.utilities.ScreenUtil.DEVICE_TYPE_PHONE) {
            currentActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        viewModel.handleIntent(StoryUserIntent.CloseVideo)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Dynamic font size and spacing based on screen width and orientation
        val baseFontSize = when {
            screenWidth >= 800.dp && isLandscape -> 32.sp
            screenWidth >= 600.dp -> 24.sp
            else -> 16.sp
        }

        val itemSpacing = when {
            screenWidth >= 800.dp && isLandscape -> 40.dp
            screenWidth >= 600.dp -> 32.dp
            else -> 20.dp
        }

        val columns = if (isLandscape) 3 else 1
        val itemWidth = if (isLandscape) (screenWidth - 16.dp - (itemSpacing * 2)) / 3 else screenWidth - 16.dp
        val thumbnailHeight = (itemWidth * 0.56f).coerceIn(VIDEO_THUMBNAIL_MIN_HEIGHT, VIDEO_THUMBNAIL_MAX_HEIGHT)
        val spinnerSize = (screenWidth * SPINNER_SCALE).coerceIn(SPINNER_MIN_SIZE, SPINNER_MAX_SIZE)

        Column(modifier = Modifier.fillMaxSize()) {
            if (state.selectedVideoId != null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    YouTubePlayer(
                        videoId = state.selectedVideoId!!,
                        lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(16 / 9f, matchHeightConstraintsFirst = isLandscape),
                        onFullscreenChange = { isFullScreen ->
                            viewModel.handleIntent(StoryUserIntent.UpdateFullScreen(isFullScreen))
                        }
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(itemSpacing),
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing)
                ) {
                    items(state.videos) { video ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable {
                                    viewModel.handleIntent(StoryUserIntent.PlayVideo(video.id))
                                }
                        ) {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = video.title,
                                placeholder = painterResource(R.drawable.video_image),
                                error = painterResource(R.drawable.video_image),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(thumbnailHeight),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = baseFontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    if (state.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(spinnerSize),
                                    color = Color.White,
                                    strokeWidth = (spinnerSize / 10).coerceAtLeast(4.dp)
                                )
                            }
                        }
                    }
                }

                InputArea(
                    modifier = Modifier.wrapContentHeight(),
                    inputText = state.inputText,
                    isListening = state.isListening,
                    hasPermission = state.hasRecordAudioPermission,
                    fontSize = baseFontSize,
                    onInputChange = { viewModel.handleIntent(BaseUserIntent.UpdateInput(it)) },
                    onSendClick = { viewModel.handleIntent(StoryUserIntent.GetStories) },
                    onMicClick = { viewModel.handleIntent(BaseUserIntent.ToggleVoiceInput) }
                )
            }
        }
    }
}
