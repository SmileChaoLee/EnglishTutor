package com.smile.englishtutor.views

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdView
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.ui.ChatScreen
import com.smile.englishtutor.ui.theme.EnglishTutorTheme
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.ChatViewModel
import com.smile.smilelibraries.AdMobBanner
import com.smile.smilelibraries.utilities.ScreenUtil

class ChatActivity : ComponentActivity() {

    companion object {
        private const val TAG ="ChatActivity"
        private const val BANNER_AD_ID = "ca-app-pub-8354869049759576/4882297130"
    }

    private var hasRecordAudioPermission = false
    private var option: Int = 0
    private lateinit var viewModel: ChatViewModel

    @SuppressLint("ConfigurationScreenWidthHeight")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LogUtil.d(TAG, "onCreate.savedInstanceState = $savedInstanceState")
        hasRecordAudioPermission = false
        option = 0
        if (savedInstanceState == null) {
            intent?.let {
                val extras = it.extras
                extras?.let { extras ->
                    hasRecordAudioPermission = extras.getBoolean(Constants.HAS_PERMISSION, false)
                    option = extras.getInt(Constants.OPTION, 0)
                }
            }
        } else {
            hasRecordAudioPermission = savedInstanceState.getBoolean(Constants.HAS_PERMISSION, false)
            option = savedInstanceState.getInt(Constants.OPTION, 0)
        }
        LogUtil.d(TAG, "onCreate.ViewModelProvider")
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(application, option) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
        viewModel.handleIntent(ChatUserIntent.UpdatePermissionStatus(hasRecordAudioPermission))

        setContent {
            LogUtil.d(TAG, "onCreate.setContent")
            EnglishTutorTheme {
                val adWidth = with(LocalDensity.current) {
                    (LocalWindowInfo.current.containerSize.width
                        .toDp().value*0.90f).toInt()
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ChatScreen(
                            modifier = Modifier.weight(1f),
                            viewModel = viewModel
                        )
                        ShowAdmobBanner(
                            modifier = Modifier.fillMaxWidth(),
                            bannerID = BANNER_AD_ID,
                            width = adWidth
                        )
                    }
                }
            }
        }

        LogUtil.d(TAG, "onCreate.ScreenUtil.getDeviceType")
        val deviceType = ScreenUtil.getDeviceType(this@ChatActivity)
        LogUtil.d(TAG, "onCreate.requestedOrientation")
        requestedOrientation = if (deviceType == ScreenUtil.DEVICE_TYPE_PHONE) {
            // phone then change orientation to Portrait
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // Table then change orientation to Landscape
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        LogUtil.d(TAG, "onSaveInstanceState()")
        outState.putBoolean(Constants.HAS_PERMISSION, hasRecordAudioPermission)
        outState.putInt(Constants.OPTION, option)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    @Composable
    fun ShowAdmobBanner(modifier: Modifier = Modifier,
                        bannerID: String, width: Int = 0) {
        LogUtil.d(TAG, "ShowAdmobBanner.bannerID = $bannerID")
        if (bannerID.isEmpty()) return
        AndroidView(
            modifier = modifier,
            factory = { context ->
                AdView(context)
            },
            update = { adView ->
                AdMobBanner(adView, bannerID, width)
            }
        )
    }
}
