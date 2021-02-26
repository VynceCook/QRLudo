package fr.angers.univ.qrludo.engines

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Tone Engine Singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * Play a tone sequence given by its name. The QRLudo tones
 * are sequence of Android simple tones (with duration and delays
 * between single simple tones)
 */
object ToneEngine {
    /// List of tone names
    enum class TONE_NAME {
        ERROR,
        START_DETECTION,
        START_SR,

        IGNORED_QR,
        FAMILY_QR,
        SET_QR,
        LAST_QR_READ,
        FIRST_QR_READ
    }
    enum class ENGINE_STATE {
        IDLE,         // Ready to handle new event
        IS_PLAYING    // The tone is playing
    }

    private var _state = ENGINE_STATE.IDLE;

    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("ToneEngine", msg, level)
    }

    fun is_idle() : Boolean {
        return (_state == ENGINE_STATE.IDLE)
    }

    // Call the Android tone player to play single tone
    private fun play_tone(tone_generator : ToneGenerator, tone_type : Int, duration_ms : Long, call_after_tone_completed: () -> Unit, optional_delay_after : Long = 0)
    {
        tone_generator.startTone(tone_type, duration_ms.toInt())
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                call_after_tone_completed()
            }
        }, duration_ms + optional_delay_after)
    }

    // Play the tone_name tone.
    // The user_call_after_complete callback is called when the tone has been player.
    // Used to link to the next action.
    fun play(tone_name: TONE_NAME, user_call_after_complete: () -> Unit = { -> }) {
        // Return if a speech recognition engine is recording or if a sound file is playing
        if (!SpeechRecognitionEngine.is_idle() || !MediaPlayerEngine.is_idle()) {
            logger(context().getString(R.string.tone_something_in_progress), Logger.DEBUG_LEVEL.INFO)
            return
        }
        if (_state != ENGINE_STATE.IDLE) {
            logger(context().getString(R.string.tone_already_playing), Logger.DEBUG_LEVEL.DEBUG)
            return
        }

        _state = ENGINE_STATE.IS_PLAYING
        var tone_generator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        var call_after_complete = { ->
            logger(context().getString(R.string.tone_played) + "(" + tone_name.name + ")", Logger.DEBUG_LEVEL.VERBOSE)
            _state = ENGINE_STATE.IDLE
            user_call_after_complete()
        }

        logger(context().getString(R.string.tone_playing) + "(" + tone_name.name + ")", Logger.DEBUG_LEVEL.VERBOSE)
        // Play a tone depending on its name
        // A tone is a sequence of basic tones
        when (tone_name) {
            TONE_NAME.ERROR -> {
                play_tone(tone_generator, ToneGenerator.TONE_SUP_ERROR, 15, call_after_complete)
            }
            TONE_NAME.IGNORED_QR -> {
                play_tone(tone_generator, ToneGenerator.TONE_SUP_ERROR, 15,
                    { ->
                        play_tone(tone_generator, ToneGenerator.TONE_SUP_ERROR, 15, call_after_complete)
                    },
                    85
                )
            }
            TONE_NAME.START_DETECTION -> {
                play_tone(tone_generator, ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 500, call_after_complete)
            }
            TONE_NAME.START_SR -> {
                play_tone(tone_generator, ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 500, call_after_complete)
            }

            TONE_NAME.FAMILY_QR -> {
                play_tone(tone_generator, ToneGenerator.TONE_CDMA_MED_SLS, 50, call_after_complete)
            }
            TONE_NAME.SET_QR -> {
                play_tone(tone_generator, ToneGenerator.TONE_CDMA_CALLDROP_LITE, 50, call_after_complete)
            }
            TONE_NAME.LAST_QR_READ -> {
                play_tone(tone_generator, ToneGenerator.TONE_CDMA_MED_PBX_SLS, 25,
                    { ->
                        play_tone(tone_generator, ToneGenerator.TONE_CDMA_MED_PBX_SLS, 25, call_after_complete)
                    },
                    75
                )
            }
            TONE_NAME.FIRST_QR_READ -> {
                play_tone(tone_generator, ToneGenerator.TONE_CDMA_MED_PBX_SLS, 25, call_after_complete)
            }
        }
    }
}