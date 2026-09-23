package com.smile.englishtutor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import com.google.android.gms.ads.AdView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.smilelibraries.AdMobBanner

@Composable
fun MyTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null
) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        title = { Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = Color(0xFF00FF00),
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
fun YouTubePlayer(
    videoId: String,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier,
    onFullscreenChange: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var c = context
        while (c is android.content.ContextWrapper) {
            if (c is Activity) {
                break
            }
            c = c.baseContext
        }
        c as? Activity
    }
    val deviceType = remember(activity, context) {
        val act = activity ?: (context as? Activity)
        act?.let { com.smile.smilelibraries.utilities.ScreenUtil.getDeviceType(it) } ?: com.smile.smilelibraries.utilities.ScreenUtil.DEVICE_TYPE_PHONE
    }
    val isPhone = deviceType == com.smile.smilelibraries.utilities.ScreenUtil.DEVICE_TYPE_PHONE

    var fullscreenViewToShow by remember { mutableStateOf<View?>(null) }
    var shouldResumePlayback by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                YouTubePlayerView(context = ctx).apply {
                    enableAutomaticInitialization = false
                    lifecycleOwner.lifecycle.addObserver(this)
                    
                    addFullscreenListener(object : FullscreenListener {
                        override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                            shouldResumePlayback = true
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            fullscreenViewToShow = fullscreenView
                            onFullscreenChange?.invoke(true)
                        }

                        override fun onExitFullscreen() {
                            val currentActivity = activity ?: (ctx as? Activity)
                            val currentDeviceType = currentActivity?.let { com.smile.smilelibraries.utilities.ScreenUtil.getDeviceType(it) }
                            activity?.requestedOrientation = if (currentDeviceType == com.smile.smilelibraries.utilities.ScreenUtil.DEVICE_TYPE_PHONE) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            }
                            fullscreenViewToShow = null
                            onFullscreenChange?.invoke(false)
                        }
                    })

                    val options = IFramePlayerOptions.Builder(ctx)
                        .controls(1)
                        .fullscreen(if (!isPhone) 0 else 1)
                        .build()

                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.loadVideo(videoId, 0f)
                            /*
                            if (isPhone) {
                                post {
                                    this@apply.matchParent()
                                }
                            }
                            */
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            if (shouldResumePlayback && state == PlayerConstants.PlayerState.PAUSED) {
                                youTubePlayer.play()
                                shouldResumePlayback = false
                            } else if (state == PlayerConstants.PlayerState.PLAYING) {
                                shouldResumePlayback = false
                            }
                        }
                    }, options)
                }
            },
            onRelease = { view ->
                if (isPhone) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                view.release()
            }
        )

        fullscreenViewToShow?.let { view ->
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    (view.parent as? android.view.ViewGroup)?.removeView(view)
                    view
                }
            )
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
            .padding(8.dp),
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
    /*
    // do not use the following because the app crashes when the orientation changes
    val adWidth = with(LocalDensity.current) {
        (LocalWindowInfo.current.containerSize.width
            .toDp().value*0.90f).toInt()
    }
    */
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context)
        },
        update = { adView ->
            AdMobBanner(adView, bannerID, 0)
        }
    )
}