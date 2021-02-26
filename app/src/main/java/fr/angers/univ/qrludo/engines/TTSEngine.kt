package fr.angers.univ.qrludo.engines

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.MultiAutoCompleteTextView
import fr.angers.univ.qrludo.utils.MainApplication
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarBool
import fr.angers.univ.qrludo.utils.Logger
import java.io.File
import java.util.*

/**
 * Text to speech Engine Singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * The text to speech engine initialize the TTS Android object and
 * can be used to synthesize text to mp3 audio files. It isn't playing
 * the file directly, one must call the MediaPlayer to play the sound file.
 */
object TTSEngine {
    // The states of the Engine
    enum class ENGINE_STATE {
        OFF,             // The engine is off, need to be started to go in IDLE state
        IDLE,            // Ready to handle new event
        IS_SYNTHESIZING, // Is synthesizing the current given text
    }

    private var _state = ENGINE_STATE.OFF
    private var _tts_engine: TextToSpeech? = null
    private lateinit var _call_after_complete : (String) -> Unit

    // Start the engine by setting the Listeners
    fun init_engine() {
        // Listener called when the TTS Android engine has been initialised
        _tts_engine = TextToSpeech(MainApplication.application_context(),
            TextToSpeech.OnInitListener { status ->
                logger(context().getString(R.string.tts_initialized) + ", status = " + status, Logger.DEBUG_LEVEL.DEBUG)
                var locale = Locale.getDefault(Locale.Category.DISPLAY)
                _tts_engine?.setLanguage( locale )
                _state = ENGINE_STATE.IDLE
                if (status == TextToSpeech.SUCCESS)
                    CoreEngine.insert(EngineVarBool("_CORESYS_QR_initialized", true))
            }
        )

        // Listener called when on job event (started, aborted, done)
        _tts_engine!!.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    logger(context().getString(R.string.tts_job_done) + " (" + utteranceId+ ")", Logger.DEBUG_LEVEL.INFO)
                    on_job_done(utteranceId!!)
                }
                override fun onError(utteranceId: String?) {
                    logger(context().getString(R.string.tts_job_error) + " (" + utteranceId+ ")", Logger.DEBUG_LEVEL.ERROR)
                }
                override fun onStart(utteranceId: String?) {
                    logger(context().getString(R.string.tts_job_start) + " (" + utteranceId+ ")", Logger.DEBUG_LEVEL.INFO)
                }
            }
        )
    }

    private fun context() : Context
    {
        return MainApplication.application_context()
    }

    private fun logger(msg : String, level : Logger.DEBUG_LEVEL)
    {
        Logger.log("TTSEngine", msg, level)
    }

    // Translate the text msg to a file.
    // The call_after_complete is called when the file has been synthesized. It can
    // be used to start the MediaPlayer after the file creation
    fun text_to_file(msg: String, call_after_complete: (String) -> Unit)
    {
        logger(context().getString(R.string.tts_speak) + " : " + msg, Logger.DEBUG_LEVEL.INFO)
        if (_state == ENGINE_STATE.OFF)
        {
            logger(context().getString(R.string.tts_not_ready), Logger.DEBUG_LEVEL.ERROR)
            call_after_complete(msg)
            return
        }
        if (_state != ENGINE_STATE.IDLE)
        {
            logger(context().getString(R.string.tts_job_already_in_progress), Logger.DEBUG_LEVEL.INFO)
            call_after_complete(msg)
            return
        }
        val media_files_path = MainApplication.get_media_files_path()
        if (media_files_path == null) { // Check that media_file directory still exists
            call_after_complete(msg)
            return
        }
        _state = ENGINE_STATE.IS_SYNTHESIZING

        _call_after_complete = call_after_complete
        // Create file name
        var msg_hash_code = msg.hashCode().toString()

        var file = File(media_files_path + "tts_$msg_hash_code.mp3")
        file.setReadable(true, false)

        if (file.exists()) {
            logger(context().getString(R.string.tts_file_exists), Logger.DEBUG_LEVEL.INFO)
            on_job_done(msg_hash_code)
        } else
            _tts_engine?.synthesizeToFile(msg, Bundle(), file, msg_hash_code)
    }

    // Start a list of text synthesizing, all files are synthesized before calling
    // the call_after_complete callback
    fun texts_to_files(msg_list: MutableList<String>, call_after_complete: () -> Unit)
    {
        if (msg_list.isEmpty())
            call_after_complete()
        else {
            text_to_file(msg_list.removeFirst(), { _ ->
                texts_to_files(msg_list, call_after_complete)
            })
        }
    }

    // Call when the job has ended with a success
    fun on_job_done(utterandId : String)
    {
        _state = ENGINE_STATE.IDLE
        _call_after_complete(MainApplication.get_media_files_path() + "tts_$utterandId.mp3")
    }
}

