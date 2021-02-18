package fr.angers.univ.qrludo.engines.coreatoms

import android.content.Context
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.coreatoms.actions.EngineAction
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Class used to model a rule of the program.
 *
 * A rule is given by a head and a body (head --> body).
 * The head is composed of HeadAtom. Each HeadAtom corresponds to an EngineVar description (name, value).
 * When each HeadAtom matches a variable of the store of variables of the CoreEngine, the body is triggered.
 * The body is composed by a list of Actions. When the body is triggered, all actions are executed one
 * by one.
 */
class EngineRule(name : String = "") {
    /**
     * HeadAtom
     *
     * A HeadAtom is used to try to match a variable of the store of variables of the CoreEngine
     * The function match() checks if the given input variable matches the current HeadAtom
     *
     * v is the EngineVar used as model to create the HeadAtom
     * check_name_only, if true, only the name of the variable is checked. If false, it
     * checks the name AND the value.
     */
    class HeadAtom(v : EngineVar, check_name_only : Boolean) {
        val _name : String = v._name
        var _value : EngineVar? = null

        init {
            if (!check_name_only)
                _value = v
        }

        // Returns a string representation of an atom
        fun to_string() : String {
            if (_value == null)
                return _name
            else
                return _value!!.to_string()
        }

        /**
         * Try to match the EngineVar \a o with the current HeadAtom.
         * Depending on the check_name_only parameter, it checks only the
         * name of the variable or (the name AND the value)
         */
        fun match(o : EngineVar): Boolean {
            if (_value == null)
                return (_name == o._name)
            else
                return ((_name == o._name) && (_value == o))
        }
    }

    private val _name : String = name       /// The name of the rule
    private var _head : MutableList<HeadAtom> = mutableListOf<HeadAtom>() /// The list of HeadAtom which compose the Head of the Rule
    private var _body : MutableList<EngineAction> = mutableListOf<EngineAction>() /// The list of Action which compose the Body of the Rule

    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("CoreEngine(Rule)", msg, level)
    }

    // Returns a string representation of a rule
    fun to_string() : String {
        var str : String = ""
        var first : Boolean = true

        if (_name.isNotEmpty())
            str += _name + " @ "

        for (atom in _head)
            if (first) {
                first = false
                str += atom.to_string()
            } else
                str += ", " + atom.to_string()

        str += " --> "

        first = true
        for (action in _body)
            if (first) {
                first = false
                str += action.to_string()
            } else
                str += ", " + action.to_string()

        return str
    }

    // Add an atom to the head of the rule
    fun add_head_atom(v : EngineVar, check_name_only: Boolean = false) {
        for (atom in _head)
            if (atom._name == v._name)
            {
                logger(context().getString(R.string.core_rule_head_atom_already_exists) + " : " + v._name ,Logger.DEBUG_LEVEL.VERBOSE)
                return
            }

        _head.add(HeadAtom(v,check_name_only))
    }

    // Add an action to the body of the rule
    fun add_action(vararg actions : EngineAction) {
        for (a in actions)
            _body.add(a)
    }

    // Check if the rule has an atom of this name
    fun check_head(atom_name : String) : Boolean {
        for (atom in _head)
            if (atom._name == atom_name)
                return true
        return false
    }

    // Apply an action and recursively call the next one. The next one is called thanks to the call_back_on_finish
    // parameter. It used to link the Actions which each other.
    private fun rec_execute_action(idx : Int, var_head : MutableList<EngineVar>, call_back_on_finish : () -> Unit)
    {
        if (idx == _body.size) {
            call_back_on_finish()
        } else {
            logger(context().getString(R.string.core_execute_action) + " : " + _body[idx].to_string(), Logger.DEBUG_LEVEL.INFO)
            _body[idx].execute( var_head, {
                rec_execute_action(idx + 1, var_head, call_back_on_finish)
            } )
        }
    }

    // Check and apply the rule is possible (i.e. if each atom of the head matches one variable of
    // the store of variables of the CoreEngine)
    fun check_and_apply(var_set : MutableMap<String, EngineVar>, call_back_on_finish : () -> Unit) {
        var var_head : MutableList<EngineVar> = mutableListOf<EngineVar>()

        logger(context().getString(R.string.core_check_rule) + " : " + to_string(), Logger.DEBUG_LEVEL.INFO)
        // Check if head rule match the var_set (i.e. all atoms of the head
        // match an element of the var_set
        for (atom in _head)
        {
            val res = var_set[atom._name]
            if ((res == null) || (!atom.match(res))) {
                call_back_on_finish()
                return
            }
            var_head.add(res)
        }

        logger(context().getString(R.string.core_apply_rule) + " : " + to_string(), Logger.DEBUG_LEVEL.INFO)
        rec_execute_action(0, var_head, call_back_on_finish)
    }
}