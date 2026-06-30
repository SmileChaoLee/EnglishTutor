package com.smile.englishtutor.utilities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceToTextManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningStatusChange: (Boolean) -> Unit
) : RecognitionListener {

    companion object {
        private const val TAG = "VoiceToTextManager"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        val logStr = "startListening"
        LogUtil.d(TAG, logStr)
        /*
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            LogUtil.e(TAG, "$logStr.Speech Recognition Service is not available on this device.")
            onError("Speech recognition is not available on this device.")
            return
        }
        */
        LogUtil.d(TAG, "$logStr.speechRecognizer = $speechRecognizer")
        if (speechRecognizer == null) {
            // Force use of Google's Speech Recognition Service which is standard on emulators
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)
            } catch (e: Exception) {
                // Fallback to default if specific component fails
                LogUtil.e(TAG, "$logStr.Exception", e)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            // HINT: Tell the system to wait longer before deciding speech has ended
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
        }

        try {
            speechRecognizer?.startListening(intent)
            onListeningStatusChange(true)
        } catch (e: Exception) {
            LogUtil.e(TAG, "$logStr.Exception.Failed to start listening", e)
            onError("Could not start speech service.")
        }
    }

    fun stopListening() {
        LogUtil.d(TAG, "stopListening")
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            LogUtil.e(TAG, "Error stopping listener", e)
        }
        onListeningStatusChange(false)
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        onListeningStatusChange(false)
    }

    override fun onError(error: Int) {
        LogUtil.e(TAG, "onError: $error")
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error (System service may have crashed)"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Speech recognition error ($error)"
        }

        // If the client service crashed, null out the recognizer so it recreates next time
        if (error == SpeechRecognizer.ERROR_CLIENT) {
            destroy()
        }

        onError(errorMessage)
        onListeningStatusChange(false)
    }

    override fun onResults(results: Bundle?) {
        LogUtil.d(TAG, "onResults")
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onResult(matches[0])
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onResult(matches[0])
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
