package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.TTSEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarInt
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString
import fr.angers.univ.qrludo.utils.FileDownloader
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import java.io.File

/**
 * Action to play a media file.
 *
 * Before being played, the media file is downloaded if it is not found
 * on the device.
 *
 * url, it is the url used to download the sound file
 * interrupt_media_player, if true, we interrupt the current playing, if false, we play the file only if the MediaPlayer is idle
 * dst_file_name, it is the name of the file on the device (if dst_file_name is null, we create a unique file name based on the url)
 * url_is_var_name, if true, the url is parameter is in fact the name of a variable of the store of variables which must be used to get the url of the sound file
 * if url_is_var_name is true, url is the var name to use to get file url to play
 */
class ActionPlayMediaURL( url : String, interrupt_media_player : Boolean = true, dst_file_name : String? = null, url_is_var_name : Boolean = false) : EngineAction {
    override val _name: String = "Play"
    val _url : String = url
    val _interrupt_media_player : Boolean = interrupt_media_player
    val _dst_file_name : String? = dst_file_name
    val _url_is_var_name : Boolean = url_is_var_name

    override fun to_string(): String {
        if (_url_is_var_name)
            return super.to_string() + "(Var_" + _url + ")"
        else
            return super.to_string() + "(" + _url + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        var real_url = _url
        // if url_is_var_name is true, we search for the variable in the store of variable which contains
        // the url to use
        if (_url_is_var_name)
        {
            var found : Boolean = false
            for (v in var_list) {
                if ((v._name == _url) && (v is EngineVarString) && v.value_to_string().isNotEmpty()) {
                    found = true
                    real_url = v.value_to_string()
                    break
                }
            }
            if (found == false) {
                Logger.log("ActionPlay", MainApplication.application_context().getString(R.string.core_action_link_error),
                    Logger.DEBUG_LEVEL.INFO)
                call_back_on_finish()
                return
            }
        }

        if (real_url.isEmpty()) {
            Logger.log("ActionPlay", MainApplication.application_context().getString(R.string.core_action_link_empty_error),
                Logger.DEBUG_LEVEL.INFO)
            CoreEngine.insert(EngineVarInt("QR_code_error", 2), call_back_on_finish)
            return
        }

        val dst_full_file_name = _dst_file_name ?: FileDownloader().encode_url(real_url)
        val file = File(dst_full_file_name)
        file.setReadable(true, false)

        // File already exists, skip download part
        if (file.exists()) {
            // Interrupt previous MediaPlayer call
            if (_interrupt_media_player)
                MediaPlayerEngine.stop()
            MediaPlayerEngine.play(
                dst_full_file_name,
                { s: String -> CoreEngine.insert(EngineVarString("MPE_end", s), call_back_on_finish) })
        } else {
            TTSEngine.text_to_file(MainApplication.application_context().getString(R.string.beginner_help_file_download_start),
                { file_name: String ->
                    if (!MainApplication.Expert_Mode) {
                        MediaPlayerEngine.stop()
                        MediaPlayerEngine.play(
                            file_name, { _: String -> })
                    }

                    // We do the work in the callback to be sure that TTSEngine is IDLE
                    FileDownloader().download_file(real_url,
                        MainApplication.get_media_files_path() + dst_full_file_name,
                        { file_name2: String ->
                            // Interrupt previous MediaPlayer call
                            if (_interrupt_media_player)
                                MediaPlayerEngine.stop()
                            MediaPlayerEngine.play(
                                file_name2,
                                { s: String -> CoreEngine.insert(EngineVarString("MPE_end", s), call_back_on_finish) })
                        })
                })
        }
    }
}