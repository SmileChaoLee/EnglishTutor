package com.smile.englishtutor.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
import com.smile.englishtutor.BuildConfig
import com.smile.smilelibraries.utilities.UmpUtil
import com.smile.englishtutor.R
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.ui.theme.EnglishTutorTheme
import com.smile.englishtutor.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val RECORD_AUDIO_REQUEST_CODE = 101
    }

    private var mFontSize: TextUnit = 0.sp
    private var toastFontSize: TextUnit = 0.sp
    private var screenSize = Point(0, 0)
    private lateinit var chatActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var storyActivityLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")
    private val backgroundColor = Color(0xffd4d28f)
    private val buttonBackground = Color.Transparent
    private val buttonContentColor = Color.Green
    private val buttonContainerColor = Color.Blue
    private var isBackPressedEnabled = true
    private var isConversationEnabled by mutableStateOf(true)
    private var isGrammarEnabled by mutableStateOf(true)
    private var isTranslationEnabled by mutableStateOf(true)
    private var isStoryEnabled by mutableStateOf(true)
    private var hasRecordAudioPermission: Boolean = false

    @SuppressLint("ConfigurationScreenWidthHeight",
        "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@MainActivity)
        val toastTextSize = textFontSize * 0.7f
        mFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp
        screenSize = ScreenUtil.getScreenSize(this@MainActivity)

        super.onCreate(savedInstanceState)

        chatActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "chatActivityLauncher.result = $result")
            loadingMessage.value = ""
            enableMainButtons()
        }

        storyActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "storyActivityLauncher.result = $result")
            loadingMessage.value = ""
            enableMainButtons()
        }

        disableExitApp()
        disableMainButtons()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LogUtil.d(TAG,"onCreate.setContent")
            EnglishTutorTheme {
                Scaffold { innerPadding ->
                    Box(
                        Modifier.padding(innerPadding)
                            .background(color = backgroundColor)
                    ) {
                        DisplayLoading(
                            loadingMessage,
                            backgroundColor,
                            getString(R.string.loadingStr)
                        )
                        CreateMainUI()
                    }
                }
            }
            LaunchedEffect(Unit) {
                dataConsentRequest()
            }
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "handleOnBackPressed")
                    exitApp()
                }
            })

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
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
    }

    private fun checkPermissions() {
        hasRecordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasRecordAudioPermission) {
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
            hasRecordAudioPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun enableExitApp() {
        isBackPressedEnabled = true
    }

    private fun disableExitApp() {
        isBackPressedEnabled = false
    }

    private fun exitApp() {
        LogUtil.d(TAG, "exitApp.isBackPressedEnabled = $isBackPressedEnabled")
        if (isBackPressedEnabled) finish()
    }

    private fun enableMainButtons() {
        isConversationEnabled = true
        isGrammarEnabled = true
        isTranslationEnabled = true
        isStoryEnabled = true
    }

    private fun disableMainButtons() {
        isConversationEnabled = false
        isGrammarEnabled = false
        isTranslationEnabled = false
        isStoryEnabled = false
    }

    private fun dataConsentRequest() {
        LogUtil.d(TAG, "dataConsentRequest")
        // user consent for personal data collection
        val deviceHashedId = if (BuildConfig.DEBUG) {
            // Debug version
            "B3EEABB8EE11C2BE770B684D95219ECB" // for debug test
        } else {
            // release version
            ""
        }
        UmpUtil.initConsentInformation(this@MainActivity,
            DEBUG_GEOGRAPHY_EEA,deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "dataConsentRequest.finished")
                    enableMainButtons()
                    enableExitApp()
                }
            })
    }

    private fun startConversationActivity() {
        Intent(
            this@MainActivity,
            ChatActivity::class.java
        ).also {
            disableMainButtons()
            intentExtras(it, Constants.CONVERSATION_OPTION)
            loadingMessage.value = getString(R.string.loadingStr)
            chatActivityLauncher.launch(it)
        }
    }

    private fun startGrammarActivity() {
        Intent(
            this@MainActivity,
            ChatActivity::class.java
        ).also {
            disableMainButtons()
            intentExtras(it, Constants.GRAMMAR_OPTION)
            loadingMessage.value = getString(R.string.loadingStr)
            chatActivityLauncher.launch(it)
        }
    }

    private fun startTranslationActivity() {
        Intent(
            this@MainActivity,
            ChatActivity::class.java
        ).also {
            disableMainButtons()
            intentExtras(it, Constants.TRANSLATION_OPTION)
            loadingMessage.value = getString(R.string.loadingStr)
            chatActivityLauncher.launch(it)
        }
    }

    private fun startStoryActivity() {
        Intent(
            this@MainActivity,
            StoryActivity::class.java
        ).also {
            disableMainButtons()
            intentExtras(it, Constants.STORY_OPTION)
            loadingMessage.value = getString(R.string.loadingStr)
            storyActivityLauncher.launch(it)
        }
    }

    private fun intentExtras(i: Intent, option: Int) {
        i.putExtra(Constants.HAS_PERMISSION, hasRecordAudioPermission)
        i.putExtra(Constants.OPTION, option)
    }

    @Composable
    fun ConversationButton(modifier: Modifier = Modifier,
                           buttonWidth: Float,
                           buttonHeight: Float,
                           textLineHeight: TextUnit) {
        LogUtil.d(TAG, "ConversationButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val noBarrierClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isConversationEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        noBarrierClicked.value = true
                        delay(200)
                        startConversationActivity()
                        noBarrierClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!noBarrierClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!noBarrierClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.conversationStr),
                    lineHeight = textLineHeight,
                    fontSize = mFontSize
                )
            }
        }
    }

    @Composable
    fun GrammarButton(modifier: Modifier = Modifier,
                      buttonWidth: Float,
                      buttonHeight: Float,
                      textLineHeight: TextUnit) {
        LogUtil.d(TAG, "GrammarButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val barrierClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isGrammarEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        barrierClicked.value = true
                        delay(200)
                        startGrammarActivity()
                        barrierClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!barrierClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!barrierClicked.value)
                            buttonContentColor
                        else Color.Red,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.grammarTipsStr),
                    lineHeight = textLineHeight,
                    fontSize = mFontSize
                )
            }
        }
    }

    @Composable
    fun TranslationButton(modifier: Modifier = Modifier,
                          buttonWidth: Float,
                          buttonHeight: Float,
                          textLineHeight: TextUnit) {
        LogUtil.d(TAG, "TranslationButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val bRemoverClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isTranslationEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        bRemoverClicked.value = true
                        delay(200)
                        startTranslationActivity()
                        bRemoverClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!bRemoverClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!bRemoverClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.translationStr),
                    lineHeight = textLineHeight,
                    fontSize = mFontSize
                )
            }
        }
    }

    @Composable
    fun StoryButton(modifier: Modifier = Modifier,
                    buttonWidth: Float,
                    buttonHeight: Float,
                    textLineHeight: TextUnit) {
        LogUtil.d(TAG, "StoryButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val bRemoverClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isStoryEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        bRemoverClicked.value = true
                        delay(200)
                        startStoryActivity()
                        bRemoverClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!bRemoverClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!bRemoverClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.englishStoryStr),
                    lineHeight = textLineHeight,
                    fontSize = mFontSize
                )
            }
        }
    }

    @Composable
    fun CreateMainUI() {
        LogUtil.d(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        LogUtil.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        val verSpacerWeight = 1.0f
        val horSpacerWeight = 1.0f
        var buttonWidth = maxWidth * ((10.0f - horSpacerWeight * 2.0f) / 10.0f)
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            buttonWidth /= 2.0f
        }
        LogUtil.d(TAG, "CreateMainUI.buttonWidth = $buttonWidth")
        // 1 in 5
        val buttonHeight = maxHeight * ((10.0f - verSpacerWeight * 2.0f) / 10.0f) / 5.0f
        LogUtil.d(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val textLineHeight = (toastFontSize.value + 5.0f).sp
        val orientation = resources.configuration.orientation
        LogUtil.d(TAG, "CreateMainUI.orientation = $orientation")
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ConversationButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                GrammarButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                TranslationButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                StoryButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
            }
        } else {
            // For Landscape orientation
            Row(modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ConversationButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                    GrammarButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TranslationButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                    StoryButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                }
            }
        }
    }

    @Composable
    fun DisplayLoading(
        loadingMessage: MutableState<String>,
        backgroundColor: Color,
        textContent: String
    ) {
        if (loadingMessage.value.isEmpty()) {
            return
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = textContent,
                color = Color.Blue, fontWeight = FontWeight.Bold,
                fontSize = mFontSize.times(2.0f))
        }
    }
}