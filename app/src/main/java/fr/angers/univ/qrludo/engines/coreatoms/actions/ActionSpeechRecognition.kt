package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.SpeechRecognitionEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarBool
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Action to start the Speech recognition engine. Some variables are posted in the store of
 * variables of the CoreEngine depending on the recognized text
 */
class ActionSpeechRecognition : EngineAction {
    override val _name: String = "SpeechRecognition"

    override fun to_string(): String {
        return super.to_string()
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        SpeechRecognitionEngine.start_listening(
            { s: String ->
                MainApplication.Main_Activity?.pretty_print(s)
                CoreEngine.insert(EngineVarString("SR_text", s), call_back_on_finish)
            },
            { s: String ->
                MainApplication.Main_Activity?.pretty_print(s)
            },
            { ->
                CoreEngine.insert(EngineVarBool("SR_abort", true), call_back_on_finish)
            },
            { ->
                MainApplication.Main_Activity?.pretty_print(MainApplication.application_context().getString(R.string.spre_partial_unkown_text))
                CoreEngine.insert(EngineVarBool("SR_error", true), call_back_on_finish)
            }
        )
    }
}