package com.smile.englishtutor.views

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smile.englishtutor.EnglishTutorApp
import com.smile.englishtutor.R
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.ui.MyTopAppBar
import com.smile.englishtutor.ui.ShowAdmobBanner
import com.smile.englishtutor.ui.theme.EnglishTutorTheme
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.BaseViewModel
import com.smile.smilelibraries.utilities.ScreenUtil

abstract class BaseActivity : ComponentActivity() {

    companion object {
        private const val TAG ="BaseActivity"
    }
    
    abstract fun initViewModel()
    @Composable
    abstract fun CreateMainUI(modifier: Modifier)

    protected var hasRecordAudioPermission = false
    protected var option: Int = Constants.CONVERSATION_OPTION
    protected lateinit var viewModel: BaseViewModel<*, *>

    @SuppressLint("ConfigurationScreenWidthHeight")
    override fun onCreate(savedInstanceState: Bundle?) {
        val deviceType = ScreenUtil.getDeviceType(this@BaseActivity)
        LogUtil.d(TAG, "onCreate.requestedOrientation")
        requestedOrientation = if (deviceType == ScreenUtil.DEVICE_TYPE_PHONE) {
            // phone then change orientation to Portrait
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // Table then change orientation to Landscape
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        LogUtil.d(TAG, "onCreate.savedInstanceState = $savedInstanceState")
        hasRecordAudioPermission = false
        option = Constants.CONVERSATION_OPTION
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
        LogUtil.d(TAG, "onCreate.initViewModel")
        initViewModel()

        setContent {
            LogUtil.d(TAG, "onCreate.setContent")
            EnglishTutorTheme {
                val title = when (option) {
                    Constants.CONVERSATION_OPTION -> getString(R.string.conversationStr)
                    Constants.GRAMMAR_OPTION -> getString(R.string.grammarTipsStr)
                    Constants.TRANSLATION_OPTION -> getString(R.string.translationStr)
                    Constants.STORY_OPTION -> getString(R.string.englishStoryStr)
                    else -> "Wrong option!"
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MyTopAppBar(title) },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        CreateMainUI(modifier = Modifier.weight(1f))
                        ShowAdmobBanner(
                            modifier = Modifier.fillMaxWidth(),
                            bannerID = EnglishTutorApp.ADMOB_BANNER_ID
                        )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        LogUtil.d(TAG, "onSaveInstanceState()")
        outState.putBoolean(Constants.HAS_PERMISSION, hasRecordAudioPermission)
        outState.putInt(Constants.OPTION, option)
        super.onSaveInstanceState(outState, outPersistentState)
    }
}
