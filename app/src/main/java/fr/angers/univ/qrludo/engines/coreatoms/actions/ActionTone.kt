package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.ToneEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString

/**
 * Action which plays a Tone by calling the ToneEngine
 *
 * tone, tone is a given tone name to play
 */
class ActionTone( tone : ToneEngine.TONE_NAME ) : EngineAction {
    override val _name: String = "Tone"
    val _tone : ToneEngine.TONE_NAME = tone

    override fun to_string(): String {
        return super.to_string() + "(" + _tone.name + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        ToneEngine.play(
            _tone,
            { -> call_back_on_finish() })
    }
}