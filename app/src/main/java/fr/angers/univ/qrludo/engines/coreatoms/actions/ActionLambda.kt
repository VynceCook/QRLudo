package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.coreatoms.EngineVar

/**
 * Action used to wrap a kotlin user function.
 * This action is a kind of generic one, it can be used
 * to prevent creating a Kotlin class for an action used only
 * once in the application.
 */
class ActionLambda( name : String, lambda_fun : (MutableList<EngineVar>, () -> Unit) -> Unit ) : EngineAction {
    override val _name: String = name
    val _lambda_fun = lambda_fun

    override fun to_string(): String {
        return _name + "()"
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        _lambda_fun(var_list, call_back_on_finish)
    }
}