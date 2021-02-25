package fr.angers.univ.qrludo.engines

import android.content.Context
import android.content.Intent
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import java.util.*

/**
 * SpeechRecognition Engine Singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * The speech recognition engine initialize the Speech Recognition
 * system and can be used to recognize some text that the user said.
 * The SpeechRecognitionEngine implements the RecognitionListener
 */
object SpeechRecognitionEngine : RecognitionListener {
    // The states of the Engine
    enum class ENGINE_STATE {
        IDLE,         /// Not yet ready
        READY,        /// Ready to handle new event
        IS_RECORDING  /// Recording any sound in order to detect some sentences
    }

    private var _state = ENGINE_STATE.IDLE;
    private lateinit var _speech_recognizer : SpeechRecognizer
    private lateinit var _call_after_complete : (String) -> Unit /// Callback called when a new sentence has been recognized
    private lateinit var _call_after_partial : (String) -> Unit  /// Callback called when a partial sentence has been recognized (not very helpful)
    private lateinit var _call_after_cancel : () -> Unit         /// Callback called when the speech recognition has been canceled
    private lateinit var _call_after_error : () -> Unit          /// Callback called when an error has been encountered during the recording step

    private fun logger(msg : String, level : Logger.DEBUG_LEVEL)
    {
        Logger.log("SpeechRecognitionEngine", msg, level)
    }

    private fun context() : Context
    {
        return MainApplication.application_context()
    }

    // Init the speech recognition engine
    private fun init_engine() {
        _state = ENGINE_STATE.IDLE
        _speech_recognizer = SpeechRecognizer.createSpeechRecognizer(context())
        if (!SpeechRecognizer.isRecognitionAvailable(context()))
        {
            logger(context().getString(R.string.spre_unavailable), Logger.DEBUG_LEVEL.ERROR)
        } else {
            logger(context().getString(R.string.spre_available), Logger.DEBUG_LEVEL.VERBOSE)
            // Set up the listeners
            _speech_recognizer.setRecognitionListener(this)
            _state = ENGINE_STATE.READY
        }
    }

    fun is_idle() : Boolean {
        return (_state == ENGINE_STATE.IDLE)
    }

    fun is_recording() : Boolean {
        return (_state == ENGINE_STATE.IS_RECORDING)
    }

    // Start the speech recognition step. The callback functions are given as input in order
    // to be triggered depending on the event (sentence found, abort, error, ...)
    fun start_listening(call_after_complete: (String) -> Unit, call_after_partial: (String) -> Unit, call_after_cancel: () -> Unit, call_after_error: () -> Unit) {
        if (!MediaPlayerEngine.is_idle() || !ToneEngine.is_idle()) {
            logger(context().getString(R.string.spre_media_player_in_progress),Logger.DEBUG_LEVEL.INFO)
            _call_after_error()
            return
        }
        // Init the engine first
        init_engine()

        if (_state == ENGINE_STATE.IDLE)
        {
            logger(context().getString(R.string.spre_not_available),Logger.DEBUG_LEVEL.ERROR)
            _call_after_error()
            return
        }
        if (_state != ENGINE_STATE.READY)
        {
            logger(context().getString(R.string.spre_already_recording),Logger.DEBUG_LEVEL.VERBOSE)
            _call_after_error()
            return
        }

        _call_after_complete = call_after_complete
        _call_after_partial = call_after_partial
        _call_after_cancel = call_after_cancel
        _call_after_error = call_after_error
        var locale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Locale.getDefault(Locale.Category.DISPLAY)
        } else {
            Locale.getDefault()
        }
        var recognizer_intent : Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        //_recognizer_intent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.displayLanguage)
        recognizer_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        _state = ENGINE_STATE.IS_RECORDING
        _speech_recognizer.startListening(recognizer_intent)
    }

    // Abort the current speech recognition step
    fun cancel() {
        if (_state == ENGINE_STATE.IDLE)
            return

        logger(context().getString(R.string.spre_abort), Logger.DEBUG_LEVEL.INFO)
        _speech_recognizer.cancel()
        _speech_recognizer.destroy()
        _state = ENGINE_STATE.IDLE
        _call_after_cancel()
    }

    // Listener triggered when a new sentence has been recognized
    override fun onResults(results: Bundle?) {
        val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        if (matches != null) {
            for (result in matches)
            {
                text += result
                text.trimIndent()
            }
            logger(context().getString(R.string.spre_success) + " (" + text + ")", Logger.DEBUG_LEVEL.INFO)
            _speech_recognizer.destroy()
            _state = ENGINE_STATE.IDLE
            _call_after_complete(text)
        }
    }

    // Listener triggered when a partial result has been found.
    // Seems to be not very useful.
    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        if (matches != null) {
            for (result in matches)
            {
                text += result
                text.trimIndent()
            }
            logger(context().getString(R.string.spre_partial_success) + " (" + text + ")", Logger.DEBUG_LEVEL.VERBOSE)
            _call_after_partial(text)
        }
    }

    // Listener triggered when the engine is ready to record
    override fun onReadyForSpeech(params: Bundle?) {
        logger(context().getString(R.string.spre_ready_for_speech), Logger.DEBUG_LEVEL.DEBUG)
    }
    // Listener triggered when the user starts to speak
    override fun onBeginningOfSpeech() {
        logger(context().getString(R.string.spre_beginning), Logger.DEBUG_LEVEL.DEBUG)
    }
    // Listener triggered when a sound is heard by the device
    override fun onRmsChanged(rmsdB: Float) {
        logger(context().getString(R.string.spre_rms_changed), Logger.DEBUG_LEVEL.EXTRA_VERBOSE)
    }
    // Listener triggered when some transcript data has been received
    override fun onBufferReceived(buffer: ByteArray?) {
        logger(context().getString(R.string.spre_sound_received), Logger.DEBUG_LEVEL.DEBUG)
    }
    // Listener triggered when the user has finish talking
    override fun onEndOfSpeech() {
        logger(context().getString(R.string.spre_ending), Logger.DEBUG_LEVEL.DEBUG)
    }
    // Listener triggered when an error has been encountered
    override fun onError(error: Int) {
        var message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        _speech_recognizer.destroy()
        _state = ENGINE_STATE.IDLE
        logger(context().getString(R.string.spre_error) + " (" + message + ")", Logger.DEBUG_LEVEL.DEBUG)
        _call_after_error()
    }
    // Listener triggered on any event
    override fun onEvent(eventType: Int, params: Bundle?) {
    }
}