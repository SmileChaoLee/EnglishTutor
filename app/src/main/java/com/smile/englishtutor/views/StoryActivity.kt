package com.smile.englishtutor.views

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
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smile.englishtutor.EnglishTutorApp
import com.smile.englishtutor.R
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.mvi.StoryUserIntent
import com.smile.englishtutor.ui.MyTopAppBar
import com.smile.englishtutor.ui.ShowAdmobBanner
import com.smile.englishtutor.ui.StoryScreen
import com.smile.englishtutor.ui.theme.EnglishTutorTheme
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.StoryViewModel

class StoryActivity : ComponentActivity() {

    companion object {
        const val TAG = "StoryActivity"
    }

    private var hasRecordAudioPermission = false
    private lateinit var viewModel: StoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LogUtil.d(TAG, "onCreate.savedInstanceState = $savedInstanceState")
        hasRecordAudioPermission = false
        if (savedInstanceState == null) {
            intent?.let {
                val extras = it.extras
                extras?.let { extras ->
                    hasRecordAudioPermission = extras.getBoolean(Constants.HAS_PERMISSION, false)
                }
            }
        } else {
            hasRecordAudioPermission = savedInstanceState.getBoolean(Constants.HAS_PERMISSION, false)
        }
        LogUtil.d(TAG, "onCreate.ViewModelProvider")
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StoryViewModel(application) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[StoryViewModel::class.java]
        viewModel.handleIntent(StoryUserIntent.UpdatePermissionStatus(hasRecordAudioPermission))

        setContent {
            EnglishTutorTheme {
                val title = getString(R.string.englishStoryStr)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MyTopAppBar(title) }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        StoryScreen(
                            modifier = Modifier.weight(1f),
                            viewModel = viewModel
                        )
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
        super.onSaveInstanceState(outState, outPersistentState)
    }
}