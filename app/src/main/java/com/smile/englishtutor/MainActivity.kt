package com.smile.englishtutor

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdView
import com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.ui.ChatScreen
import com.smile.englishtutor.ui.theme.EnglishTutorTheme
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.ChatViewModel
import com.smile.smilelibraries.AdMobBanner
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.UmpUtil

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG ="MainActivity"
        private const val RECORD_AUDIO_REQUEST_CODE = 101
        private const val BANNER_AD_ID = "ca-app-pub-8354869049759576/4882297130"
    }

    private var touchDisabled = true
    private lateinit var viewModel: ChatViewModel

    @SuppressLint("ConfigurationScreenWidthHeight")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        touchDisabled = true
        enableEdgeToEdge()
        LogUtil.d(TAG, "onCreate.savedInstanceState = $savedInstanceState")
        LogUtil.d(TAG, "onCreate.ViewModelProvider")
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
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
        val deviceType = ScreenUtil.getDeviceType(this@MainActivity)
        LogUtil.d(TAG, "onCreate.requestedOrientation")

        requestedOrientation = if (deviceType == ScreenUtil.DEVICE_TYPE_PHONE) {
            // phone then change orientation to Portrait
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // Table then change orientation to Landscape
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        LogUtil.d(TAG, "onCreate.checkPermissions()")
        checkPermissions()

        // user consent for personal data collection
        val deviceHashedId = if (BuildConfig.DEBUG) {
            // Debug version
            "B3EEABB8EE11C2BE770B684D95219ECB" // for debug test
        } else {
            // release version
            ""
        }
        UmpUtil.initConsentInformation(this@MainActivity,
            DEBUG_GEOGRAPHY_EEA, deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "onCreate.initConsentInformation.finished")
                    // enabling receiving touch events
                    touchDisabled = false
                }
            })
    }

    private fun checkPermissions() {
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        viewModel.handleIntent(ChatUserIntent.UpdatePermissionStatus(hasPermission))

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            val hasPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            viewModel.handleIntent(ChatUserIntent.UpdatePermissionStatus(hasPermission))
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (touchDisabled) {
            // Consume the touch event, effectively disabling touch
            return true
        }
        // Allow touch events to proceed
        return super.dispatchTouchEvent(ev)
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
