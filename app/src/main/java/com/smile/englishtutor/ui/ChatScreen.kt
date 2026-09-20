package com.smile.englishtutor.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.ChatViewModel

private const val TAG = "ChatScreen"

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(state.messages.size) {
        LogUtil.d(TAG, "LaunchedEffect.state.messages.size")
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
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
                items(state.messages) { message ->
                    ChatBubble(
                        message = message,
                        isSpeaking = state.speakingMessageId == message.id,
                        fontSize = baseFontSize,
                        onSpeakClick = { viewModel.handleIntent(ChatUserIntent.SpeakText(message.id, message.text)) }
                    )
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
                onSendClick = { viewModel.handleIntent(ChatUserIntent.SendMessage) },
                onMicClick = { viewModel.handleIntent(BaseUserIntent.ToggleVoiceInput) }
            )
        }
    }
}

