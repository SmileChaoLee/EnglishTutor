package com.smile.englishtutor.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

@Composable
fun StoryScreen(
    modifier: Modifier = Modifier,
    viewModel: StoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(state.videos.size) {
        LogUtil.d(TAG, "LaunchedEffect.state.messages.size")
        if (state.videos.isNotEmpty()) {
            listState.animateScrollToItem(state.videos.size - 1)
        }
    }

    LaunchedEffect(state.error) {
        LogUtil.d(TAG, "LaunchedEffect.state.error")
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.handleIntent(BaseUserIntent.ClearError)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Dynamic font size based on screen width and orientation
        val baseFontSize = when {
            screenWidth >= 800.dp && isLandscape -> 32.sp
            screenWidth >= 600.dp -> 24.sp
            else -> 16.sp
        }

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.videos) { video ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        AsyncImage(
                            model = video.thumbnail,
                            contentDescription = video.title,
                            placeholder = painterResource(R.drawable.video_image),
                            error = painterResource(R.drawable.video_image),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((screenWidth * 0.56f).coerceIn(180.dp, 400.dp)),
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
                    item {
                        // Make spinner size relative to screen width (e.g., 15% of width)
                        // Constrained between 48dp and 120dp
                        val spinnerSize = (screenWidth * 0.15f).coerceIn(48.dp, 120.dp)
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
                onSendClick = { viewModel.handleIntent(StoryUserIntent.SendMessage) },
                onMicClick = { viewModel.handleIntent(BaseUserIntent.ToggleVoiceInput) }
            )
        }
    }
}