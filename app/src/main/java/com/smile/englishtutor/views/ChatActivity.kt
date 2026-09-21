package com.smile.englishtutor.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smile.englishtutor.models.Constants
import com.smile.englishtutor.mvi.BaseUserIntent
import com.smile.englishtutor.mvi.ChatUserIntent
import com.smile.englishtutor.ui.ChatScreen
import com.smile.englishtutor.utilities.LogUtil
import com.smile.englishtutor.viewmodels.ChatViewModel

class ChatActivity : BaseActivity() {

    companion object {
        private const val TAG ="ChatActivity"
    }

    private val englishLanguage = Constants.ENGLISH_LANGUAGE
    private var targetLanguage = Constants.ENGLISH_LANGUAGE

    override fun initViewModel() {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(application, option) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
        viewModel.handleIntent(BaseUserIntent.UpdatePermissionStatus(hasRecordAudioPermission))
        viewModel.handleIntent(ChatUserIntent.Translate(translateFrom = englishLanguage, translateTo = targetLanguage))
    }

    @Composable
    override fun CreateMainUI(modifier: Modifier) {
        if (option == Constants.TRANSLATION_OPTION) {
            TranslationBar()
        }
        ChatScreen(
            modifier = modifier,
            viewModel = viewModel as ChatViewModel
        )
    }

    private fun setTransLanguages(isEnglishTo: Boolean) {
        LogUtil.d(TAG, "setTransLanguages.isEnglishTo: $isEnglishTo")
        if (isEnglishTo) {
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
    }

    @Composable
    fun TranslationBar() {
        val logStr = "TranslationBar"
        var isEnglishToOther by remember { mutableStateOf(true) }
        var otherText by remember { mutableStateOf(targetLanguage) }
        val greenColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color(0xFF4CAF50)
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
                            setTransLanguages(isEnglishToOther)
                        },
                    label = { Text("Language") },
                    placeholder = { Text("Enter target") },
                    colors = greenColors,
                    modifier = Modifier.weight(1f)
                )
            }
            if (isEnglishToOther) {
                LogUtil.d(TAG, "$logStr.isEnglishToOther.targetLanguage = $targetLanguage")
                englishField()
                IconButton(onClick = { isEnglishToOther = !isEnglishToOther }) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Reverse")
                }
                targetField()
            } else {
                LogUtil.d(TAG, "$logStr.not EnglishToOther.targetLanguage = $targetLanguage")
                targetField()
                IconButton(onClick = { isEnglishToOther = !isEnglishToOther }) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Reverse")
                }
                englishField()
            }
            setTransLanguages(isEnglishToOther)
        }
    }
}
