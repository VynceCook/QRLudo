package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.utils.Logger

/**
 * Action to log a message on console.
 */
class ActionLog(desc : String) : EngineAction {
    override val _name: String = "Log"
    val _desc : String = desc

    override fun to_string(): String {
        return super.to_string() + "(" + _desc + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        Logger.log("Action log",_desc, Logger.DEBUG_LEVEL.INFO)
        call_back_on_finish()
    }
}