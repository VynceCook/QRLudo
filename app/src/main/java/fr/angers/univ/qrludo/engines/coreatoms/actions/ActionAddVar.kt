package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar

/**
 * Action that add a EnginveVar to store of variables of the CoreEngine.
 * When a new variable is added, the search for rules that can be triggered
 * begins.
 */
class ActionAddVar(v : EngineVar) : EngineAction {
    override val _name: String = "Add_"+ v._name
    val _var : EngineVar = v

    override fun to_string(): String {
        return "Add(" + _var.to_string() + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        CoreEngine.insert(_var,call_back_on_finish)
    }
}