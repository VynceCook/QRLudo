package fr.angers.univ.qrludo.engines

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import java.io.File

/**
 * MediaPlayer Engine Singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * The MediaPlayer engine initialize the MediaPlayer then plays
 * sound files and ensure that only one file is played at a time.
 * It forward actions like pause, stop, resume ....
 */
object MediaPlayerEngine {
    // The states of the Engine
    enum class ENGINE_STATE {
        IDLE,         // Ready to handle new event
        IS_PREPARING, // Prepare the sound file before playing
        IS_PLAYING,   // The sound file is playing
        PAUSED        // The playing is paused (can be resume later)
    }

    private var _state = ENGINE_STATE.IDLE;

    private var _media_player: MediaPlayer? = null
    private lateinit var _call_after_complete : (String) -> Unit
    private lateinit var _sound_file_name : String

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL)
    {
        Logger.log("MediaPlayerEngine", msg, level)
    }

    private fun context() : Context
    {
        return MainApplication.application_context()
    }

    fun is_idle() : Boolean {
        return (_state == ENGINE_STATE.IDLE)
    }

    fun is_playing() : Boolean {
        return (_state == ENGINE_STATE.IS_PLAYING)
    }

    // Init the MediaPlayer engine: setup the listeners
    private fun init_engine()
    {
        _media_player = MediaPlayer()
        _media_player?.setOnPreparedListener {
            logger(context().getString(R.string.sound_file_prepared), Logger.DEBUG_LEVEL.VERBOSE)
            on_sound_file_prepared()
        }
        _media_player?.setOnCompletionListener {
            logger(context().getString(R.string.sound_file_end), Logger.DEBUG_LEVEL.DEBUG)
            on_sound_file_complete()
        }
        _media_player?.setOnErrorListener(MediaPlayer.OnErrorListener { _, what, _ ->
            logger(context().getString(R.string.media_player_error) + " ID: " + what,
                Logger.DEBUG_LEVEL.ERROR)
            true
        })
        _state = ENGINE_STATE.IDLE
    }

    // Play the sound file
    // Ensure first that no SpeechRecognition, ToneEngine or another MediaFile playing
    // in progress.
    // It first initialize the engine, prepare the sound file and then
    // starts the playing.
    // call_after_complete is called once the sound file has been played. It is used
    // to link with the next action.
    fun play(file_name: String, call_after_complete: (String) -> Unit = { _ : String -> })
    {
        if (!SpeechRecognitionEngine.is_idle() || !ToneEngine.is_idle()) {
            logger(context().getString(R.string.media_player_something_in_progress),
                Logger.DEBUG_LEVEL.INFO)
            call_after_complete(file_name)
            return
        }
        if (_state != ENGINE_STATE.IDLE) {
            logger(context().getString(R.string.media_player_already_playing),
                Logger.DEBUG_LEVEL.INFO)
            call_after_complete(file_name)
            return
        }

        val file = File(file_name)
        file.setReadable(true, false)
        if (!file.exists() || (file.length() == 0L)) {
            logger(context().getString(R.string.media_player_file_not_found),
                Logger.DEBUG_LEVEL.DEBUG)
            call_after_complete(file_name)
            return
        }



        init_engine()
        _sound_file_name = file_name
        _media_player?.setAudioAttributes(AudioAttributes.Builder()
            .setFlags(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        _media_player?.setDataSource(file_name)

        val playbackParams = PlaybackParams()
        playbackParams.speed = MainApplication.Play_Speed
        _media_player?.setPlaybackParams(playbackParams)

        _state = ENGINE_STATE.IS_PREPARING;
        _call_after_complete = call_after_complete
        logger(context().getString(R.string.media_player_is_preparing), Logger.DEBUG_LEVEL.VERBOSE)
        _media_player?.prepareAsync()
    }

    // Listener called when the sound file has been prepared. After being
    // prepared, the sound file will be played.
    fun on_sound_file_prepared()
    {
        if (_state != ENGINE_STATE.IS_PREPARING)
        {
            logger(context().getString(R.string.sound_file_prepared_error),
                Logger.DEBUG_LEVEL.ERROR)
            return
        }
        logger(context().getString(R.string.sound_file_playing), Logger.DEBUG_LEVEL.VERBOSE)
        _state = ENGINE_STATE.IS_PLAYING;
        _media_player?.start()
    }

    // Listener called when the playing of the sound file is finished.
    // After being played, the call_back_after_complete is called.
    fun on_sound_file_complete()
    {
        if (_state != ENGINE_STATE.IS_PLAYING)
        {
            logger(context().getString(R.string.sound_file_completed_error),
                Logger.DEBUG_LEVEL.ERROR)
            return
        }
        _media_player?.reset()
        _media_player?.release()
        _state = ENGINE_STATE.IDLE
        _call_after_complete(_sound_file_name)
    }

    // Pause or resume the playing of the sound file.
    fun pause_or_resume()
    {
        if (_state == ENGINE_STATE.IS_PLAYING) {
            if (_media_player?.isPlaying() == false)
                return

            _state = ENGINE_STATE.PAUSED
            logger(context().getString(R.string.sound_file_paused), Logger.DEBUG_LEVEL.INFO)
            _media_player?.pause()
        } else if (_state == ENGINE_STATE.PAUSED)
        {
            _state = ENGINE_STATE.IS_PLAYING
            logger(context().getString(R.string.sound_file_playing), Logger.DEBUG_LEVEL.INFO)
            _media_player?.start()
        }
    }

    // Stop the playing and the reset()/release() the MediaPlayer engine.
    // The MediaPlayerEngine state will be IDLE and ready to play another sound file
    fun stop() {
        if (_state == ENGINE_STATE.IDLE)
            return

        logger(context().getString(R.string.sound_file_stopped), Logger.DEBUG_LEVEL.INFO)
        val backup_state = _state
        _media_player?.reset()
        _media_player?.release()
        _state = ENGINE_STATE.IDLE
        if ((backup_state == ENGINE_STATE.IS_PLAYING) || (backup_state == ENGINE_STATE.PAUSED))
            _call_after_complete(_sound_file_name)
    }

    // Replay the current sound file from the start
    fun replay_from_start() {
        if ((_state == ENGINE_STATE.IS_PLAYING) || (_state == ENGINE_STATE.PAUSED)) {
            _media_player?.seekTo(0);
            if (_state == ENGINE_STATE.PAUSED)
            {
                _state = ENGINE_STATE.IS_PLAYING
                _media_player?.start()
            }
        }
    }

    // Rewind the sound file from duration_sec seconds
    fun seek(duration_sec: Int) {
        if ((_state == ENGINE_STATE.IS_PLAYING) || (_state == ENGINE_STATE.PAUSED)) {
            val current_post = _media_player?.getCurrentPosition()
            if (current_post != null) {
                _media_player?.seekTo(maxOf(0, current_post + (1000 * duration_sec)))
            }
            if (_state == ENGINE_STATE.PAUSED)
            {
                _state = ENGINE_STATE.IS_PLAYING
                _media_player?.start()
            }
        }
    }
}