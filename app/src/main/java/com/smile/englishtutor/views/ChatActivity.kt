package com.smile.englishtutor.views

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdView
import com.smile.englishtutor.R
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
    private val englishLanguage = "English"
    private var targetLanguage = englishLanguage

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
        viewModel.handleIntent(ChatUserIntent.Translate(translateFrom = englishLanguage, translateTo = targetLanguage))

        setContent {
            LogUtil.d(TAG, "onCreate.setContent")
            EnglishTutorTheme {
                val adWidth = with(LocalDensity.current) {
                    (LocalWindowInfo.current.containerSize.width
                        .toDp().value*0.90f).toInt()
                }
                val title = when (option) {
                    0 -> getString(R.string.conversationStr)
                    1 -> getString(R.string.grammarTipsStr)
                    2 -> getString(R.string.translationStr)
                    else -> "Wrong option!"
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = Color(0xFF2E7D32),
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (option == 2) {
                            TranslationBar()
                        }
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
    fun TranslationBar() {
        var isEnglishToOther by remember { mutableStateOf(true) }
        var otherText by remember { mutableStateOf(targetLanguage) }
        val greenColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E7D32),
            unfocusedBorderColor = Color(0xFF2E7D32)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val englishField = @Composable {
                OutlinedTextField(
                    value = englishLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") },
                    colors = greenColors,
                    modifier = Modifier.weight(1f)
                )
            }
            val targetField = @Composable {
                OutlinedTextField(
                    value = otherText,
                    onValueChange =
                        {
                            otherText = it
                            targetLanguage = it
                            LogUtil.d(TAG, "targetField.targetLanguage = $targetLanguage")
                            if (isEnglishToOther) {
                                viewModel.handleIntent(
                                    ChatUserIntent.Translate(
                                        translateFrom = englishLanguage,
                                        translateTo = targetLanguage
                                    )
                                )
                            } else {
                                viewModel.handleIntent(
                                    ChatUserIntent.Translate(
                                        translateFrom = targetLanguage,
                                        translateTo = englishLanguage
                                    )
                                )
                            }
                        },
                    label = { Text("Language") },
                    placeholder = { Text("Enter target") },
                    colors = greenColors,
                    modifier = Modifier.weight(1f)
                )
            }
            if (isEnglishToOther) {
                englishField()
                IconButton(onClick = { isEnglishToOther = !isEnglishToOther }) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Reverse")
                }
                targetField()
            } else {
                targetField()
                IconButton(onClick = { isEnglishToOther = !isEnglishToOther }) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Reverse")
                }
                englishField()
            }
        }
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
