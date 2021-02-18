package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Action used to print a text in a widget of the UI.
 * It calls a callback of the MainActivity which aims
 * at doing the update of the text widget
 */
class ActionPrettyPrint(msg : String) : EngineAction {
    override val _name: String = "PrettyPrint"
    val _msg: String = msg

    override fun to_string(): String {
        return super.to_string() + "(" + _msg + ")"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        MainApplication.Main_Activity?.pretty_print(_msg)
        call_back_on_finish()
    }
}