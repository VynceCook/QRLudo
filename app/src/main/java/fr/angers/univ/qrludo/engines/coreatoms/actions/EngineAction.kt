package fr.angers.univ.qrludo.engines.coreatoms.actions

import fr.angers.univ.qrludo.engines.coreatoms.EngineVar

/**
 * This interface is the base of any action
 *
 * An action is given by a name (for logging and printing purpose).
 * The most important function is the execute() one, it is called to run (execute)
 * the action. It has to be redefined in each class which implements the EngineAction.
 */
interface EngineAction {
    val _name : String

    // Returns a string representation of an action
    fun to_string() : String {
        return _name
    }

    /**
     * Execute the action
     *
     * var_list, var_list is the list of variables that the head of the rule matched. The action
     * can find some useful data inside one of them
     * call_back_on_finish, call_back_on_finish is the callback to call when the action is finished. It
     * is used to link the action between them. Be sure to call that callback on the end of the action !
     * Check existing actions to be sure to understand what it means.
     */
    fun execute(var_list : MutableList<EngineVar>, call_back_on_finish : () -> Unit)
}