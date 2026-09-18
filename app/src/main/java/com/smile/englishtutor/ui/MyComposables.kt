package com.smile.englishtutor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView
import com.smile.englishtutor.models.ChatMessage
import com.smile.smilelibraries.AdMobBanner

@Composable
fun MyTopAppBar(title: String) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        title = { Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = Color(0xFF00FF00),
        )
    )
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    fontSize: TextUnit = 16.sp,
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
                val volumeIconSize = (fontSize.value * 1.5f).dp * 2
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier.size(volumeIconSize)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = if (isSpeaking) Color.Red else Color.White,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
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
                    fontSize = fontSize,
                    lineHeight = (fontSize.value + 3).sp
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
    fontSize: TextUnit = 16.sp,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit
) {
    val sendIconSize = (fontSize.value * 2.5f).dp
    val micIconSize = (fontSize.value * 1.5f).dp * 2
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMicClick,
            enabled = hasPermission,
            modifier = Modifier.size(micIconSize)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = if (!hasPermission) Color.DarkGray else if (isListening) Color.Red else Color.White,
                modifier = Modifier.fillMaxSize().padding(0.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f),
            textStyle = TextStyle(fontSize = fontSize),
            placeholder = { Text("Ask a question...", color = Color.Gray, fontSize = fontSize) },
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
        IconButton(
            onClick = onSendClick,
            enabled = inputText.isNotBlank(),
            modifier = Modifier.size(sendIconSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (inputText.isNotBlank()) Color.White else Color.Gray,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
        }
    }
}

@Composable
fun ShowAdmobBanner(modifier: Modifier = Modifier,
                    bannerID: String) {
    if (bannerID.isEmpty()) return
    val adWidth = with(LocalDensity.current) {
        (LocalWindowInfo.current.containerSize.width
            .toDp().value*0.90f).toInt()
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context)
        },
        update = { adView ->
            AdMobBanner(adView, bannerID, adWidth)
        }
    )
}