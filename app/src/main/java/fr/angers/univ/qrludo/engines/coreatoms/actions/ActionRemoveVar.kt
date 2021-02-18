package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar

/**
 * Action which remove a var from the store of variables of the CoreEngine.
 * As a the variable name is the key to find a variable, only the name is needed
 * to remove it from the store. Remember that a variable name can be found only one
 * time in the store of variables.
 */
class ActionRemoveVar(var_name : String) : EngineAction {
    override val _name: String = var_name

    override fun to_string(): String {
        return "Remove(" + _name + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        CoreEngine.remove(_name)
        call_back_on_finish()
    }
}