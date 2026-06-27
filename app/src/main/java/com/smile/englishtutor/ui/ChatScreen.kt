package com.smile.englishtutor.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smile.englishtutor.models.ChatMessage
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.viewmodels.ChatViewModel

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.handleIntent(ChatUserIntent.ClearError)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = maxWidth
        
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
                onInputChange = { viewModel.handleIntent(ChatUserIntent.UpdateInput(it)) },
                onSendClick = { viewModel.handleIntent(ChatUserIntent.SendMessage) },
                onMicClick = { viewModel.handleIntent(ChatUserIntent.ToggleVoiceInput) }
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) Color(0xFF3700B3) else Color(0xFF424242)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                IconButton(onClick = onSpeakClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = if (isSpeaking) Color.Red else Color.White
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    color = textColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun InputArea(
    modifier: Modifier = Modifier,
    inputText: String,
    isListening: Boolean,
    hasPermission: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMicClick, enabled = hasPermission) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = if (!hasPermission) Color.DarkGray else if (isListening) Color.Red else Color.White
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f),
            placeholder = { Text("Ask a question...", color = Color.Gray) },
            minLines = 3,
            maxLines = 5,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF212121),
                unfocusedContainerColor = Color(0xFF212121),
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSendClick, enabled = inputText.isNotBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (inputText.isNotBlank()) Color.White else Color.Gray
            )
        }
    }
}
