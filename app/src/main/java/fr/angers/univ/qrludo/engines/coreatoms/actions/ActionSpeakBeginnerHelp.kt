package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.TTSEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Use TTS engine to render a text to speech and play it thanks to the MediaPlayerEngine
 *
 * This text is said only if Expert_Mode is false !!! It used to get additionnal help
 * messages
 *
 * msg, msg is the text to speak
 * interrupt_media_player, if true, we interrupt the current playing, if false, we play the file only if the MediaPlayer is idle
 * msg_is_var_name, if msg_is_var_name is true, msg is the var name to use to get content to speak
 */
class ActionSpeakBeginnerHelp(msg : String, interrupt_media_player : Boolean = true, msg_is_var_name : Boolean = false ) : EngineAction {
    override val _name: String = "SpeakBeginnerHelp"
    val _msg : String = msg
    val _interrupt_media_player : Boolean = interrupt_media_player
    val _msg_is_var_name : Boolean = msg_is_var_name

    override fun to_string(): String {
        if (_msg_is_var_name)
            return super.to_string() + "(Var_" + _msg + ")"
        else
            return super.to_string() + "(" + _msg + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        // Speak only in not ExperMode
        if (MainApplication.Expert_Mode)
        {
            call_back_on_finish()
            return
        }

        var txt_to_speak = _msg
        // Is msg_is_var_name is true, we search for a variable in the store of variable which
        // is of the same name. The found variable will contain the text to speak.
        if (_msg_is_var_name)
        {
            var found : Boolean = false
            for (v in var_list) {
                if ((v._name == _msg) && v.value_to_string().isNotEmpty()) {
                    found = true
                    txt_to_speak = v.value_to_string()
                    break
                }
            }
            if (found == false) {
                Logger.log("ActionSpeak", MainApplication.application_context().getString(R.string.core_action_link_error),Logger.DEBUG_LEVEL.INFO)
                call_back_on_finish()
                return
            }
        }

        // We render the file and call the MediaPlayer to play it
        TTSEngine.text_to_file(txt_to_speak, { file_name: String ->
            // Interrupt previous MediaPlayer call
            if (_interrupt_media_player)
                MediaPlayerEngine.stop()
            MediaPlayerEngine.play(
                file_name,
                { s: String -> CoreEngine.insert( EngineVarString("TTS_end", s), call_back_on_finish) })
        })
    }
}