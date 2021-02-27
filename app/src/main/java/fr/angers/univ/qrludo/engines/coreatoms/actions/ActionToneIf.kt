package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.ToneEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.utils.MainApplication
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Action which plays a Tone by calling the ToneEngine only if a condition is true
 *
 * cond is an AtomicBoolean because Kotlin can't pass integral types by reference.
 * So we use an AtomicBoolean which is an object and is passed by reference.
 * cond, cond is the condtion to satisfy to play the tone
 * tone, tone is a given tone name to play
 */
class ActionToneIf(cond: AtomicBoolean, tone : ToneEngine.TONE_NAME ) : EngineAction {
    override val _name: String = "ToneIf"
    val _tone : ToneEngine.TONE_NAME = tone
    val _cond : AtomicBoolean = cond

    override fun to_string(): String {
        return super.to_string() + "(" + _tone.name + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        if (!_cond.get())
        {
            call_back_on_finish()
            return
        }
        ToneEngine.play(
            _tone,
            { -> call_back_on_finish() })
    }
}