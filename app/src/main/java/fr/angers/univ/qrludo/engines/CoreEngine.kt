package fr.angers.univ.qrludo.engines

import android.content.Context
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.coreatoms.EngineRule
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * CoreEngine (singleton)
 *
 * In Kotlin, Singleton is performed using an object.
 * CoreEngine is the main engine of the application. It stores all rules and
 * all variables inserted by the application. When a new variable is inserted
 * in the store, it searches for any applicable rule and executes them.
 *
 * The rules are called the program of the application. It is split in two sets :
 * - The user set of rules
 * - The system set of rules
 *
 * The user set of rules represents the user program which is cleared and loaded
 * each time a new QR code is red.
 * The system set of rules represents the system program which is never cleared.
 * It is used to model system default actions (clear variables, restore program states,
 * etc.)
 */
object CoreEngine {
    private var _var_engine_store : MutableMap<String, EngineVar> = mutableMapOf()
    private var _pending_triggered_vars : MutableList<String> = mutableListOf()
    private var _system_rules : MutableList<EngineRule> = mutableListOf()
    private var _user_rules : MutableList<EngineRule> = mutableListOf()

    // Backup of user rules used to backup current user program and restore it later
    private var _backup_user_rules : MutableList<EngineRule> = mutableListOf()
    private var _backup_user_vars : MutableMap<String, EngineVar> = mutableMapOf()

    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("CoreEngine", msg, level)
    }

    // Returns a string representation of the store of variables
    fun var_set_to_string() : String {
        var first : Boolean = true;
        var str : String = "{ "
        for (v in _var_engine_store.values) {
            if (first) {
                first = false
                str += v.to_string()
            } else {
                str += ", " + v.to_string()
            }
        }
        for (v in _pending_triggered_vars) {
            if (first) {
                first = false
                str += "#" + v
            } else {
                str += ", #" + v
            }
        }
        str += " }"
        return str
    }

    // Returns a "pretty" string representation of the set of rules.
    // This representation is more suitable for printing in console.
    fun rules_to_string_pretty_print() : String {
        var str: String = "_QRLudo_  {\n"
        for (r in _user_rules)
            str += "_QRLudo_    _u_<" + r.to_string() + ">\n"
        for (r in _system_rules)
            str += "_QRLudo_    _s_<" + r.to_string() + ">\n"
        str += "_QRLudo_  }"
        return str
    }

    // Returns a string representation of the set of rules
    fun rules_to_string() : String {
        var first : Boolean = true;
        var str : String = "{ "
        for (r in _user_rules) {
            if (first) {
                first = false
                str += "_u_<" + r.to_string() + ">"
            } else {
                str += ", _u_<" + r.to_string() + ">"
            }
        }
        for (r in _system_rules) {
            if (first) {
                first = false
                str += "_s_<" + r.to_string() + ">"
            } else {
                str += ", _s_<" + r.to_string() + ">"
            }
        }
        str += " }"
        return str
    }

    // Clear all user variables of the store of variables
    fun clear_user_var_store() {
        _pending_triggered_vars.clear()
        // Remove all variables except SYS ones
        val sys_var = _var_engine_store.filter { it.key.startsWith("_CORESYS_") }
        _var_engine_store.clear()
        _var_engine_store.plusAssign(sys_var)
    }

    // Clear all user rules
    fun clear_user_rules() {
        _user_rules.clear()
    }

    // Clear all system rules
    fun clear_system_rules() {
        _system_rules.clear()
    }

    // Check is a backup exists
    fun is_backup_user_rules() : Boolean {
        return _backup_user_rules.isNotEmpty()
    }

    // Clear last backup
    fun clear_backup_user_rules() {
        _backup_user_vars.clear()
        _backup_user_rules.clear()
    }

    // Backup current user set of rules
    // Backuping the current user set of rules allow the application to
    // switch between two user programs.
    fun backup_user_rules() {
        val user_vars = _var_engine_store.filter { !it.key.startsWith("_CORESYS_") }
        _backup_user_vars.clear()
        _backup_user_vars.plusAssign(user_vars)
        _backup_user_rules.clear()
        _backup_user_rules.plusAssign(_user_rules)
    }

    // Restore backup of user of rules
    // Restoring the current user set of rules allow the application to
    // switch between two user programs. It restores the previous backuped
    // program.
    fun restore_backup_user_rules() {
        clear_user_var_store()
        _var_engine_store.plusAssign(_backup_user_vars)
        _user_rules.clear()
        _user_rules.addAll(_backup_user_rules)
    }

    // Insert or Update a new EngineVar in the variables set
    // When a new variable is inserted or updated, it searches for
    // any applicable rule and executes them.
    // call_back_on_finish, the callback is used to call the next part of
    // the program when any new effect of the inserted variable has been
    // executed. It is used to link the next action.
    fun insert(v : EngineVar, call_back_on_finish : () -> Unit = { -> }) {
        // We add var now
        logger(context().getString(R.string.core_add_var) + " : " + v.to_string(), Logger.DEBUG_LEVEL.VERBOSE)
        // If the v.name is already in _var_engine_set, then it is replaced
        _var_engine_store.put(v._name, v)

        check_all_rules(v._name, { ->
            logger(context().getString(R.string.core_add_var_end) + " : " + v.to_string(), Logger.DEBUG_LEVEL.VERBOSE)
            call_back_on_finish()
        })
    }

    // Remove a EngineVar from the variables store
    fun remove(var_name : String) {
        // We remove var
        logger(context().getString(R.string.core_remove_var) + " : " + var_name, Logger.DEBUG_LEVEL.VERBOSE)
        _var_engine_store.remove(var_name)
        // Remove var from _pending_triggered_vars
        _pending_triggered_vars.remove(var_name)
    }

    // Add a rule to the system set of rules
    fun add_system_rule(rule : EngineRule) {
        _system_rules.add(rule)
    }

    // Add a rule to the user set of rules
    fun add_user_rule(rule : EngineRule) {
        _user_rules.add(rule)
    }

    // Recursively check and apply all user applicable rules
    private fun rec_check_user_rule(idx : Int, trigger_var : String, call_back_on_finish : () -> Unit) {
        if (idx >= _user_rules.size) {
            call_back_on_finish()
        } else {
            // If the triggered variable is not in the head, no need
            // to check the rule, we can fo to the next one.
            if (_user_rules[idx].check_head(trigger_var)) {
                _user_rules[idx].check_and_apply(_var_engine_store, {
                    rec_check_user_rule(idx + 1, trigger_var, call_back_on_finish)
                })
            } else {
                rec_check_user_rule(idx + 1, trigger_var, call_back_on_finish)
            }
        }
    }

    // Recursively check and apply all system applicable rules
    private fun rec_check_system_rule(idx : Int, trigger_var : String, call_back_on_finish : () -> Unit) {
        if (idx >= _system_rules.size) {
            call_back_on_finish()
        } else {
            // If the triggered variable is not in the head, no need
            // to check the rule, we can fo to the next one.
            if (_system_rules[idx].check_head(trigger_var)) {
                _system_rules[idx].check_and_apply(_var_engine_store, {
                    rec_check_system_rule(idx + 1, trigger_var, call_back_on_finish)
                })
            } else {
                rec_check_system_rule(idx + 1, trigger_var, call_back_on_finish)
            }
        }
    }

    // Check all user rules and then all system rules
    // The trigger_var parameter corresponds to the inserted variable which has
    // triggered this whole check phase.
    // The call_back_on_finish is called when all rules have been executed and all
    // corresponding effects have been dealt with. It is used to link with the next program action.
    private fun check_all_rules(trigger_var : String, call_back_on_finish : () -> Unit = { -> }) {
        rec_check_user_rule(0, trigger_var, {
            rec_check_system_rule(0, trigger_var, {
                if (!_pending_triggered_vars.isEmpty())
                {
                    // Insert pending var engine
                    val v = _pending_triggered_vars.removeFirst()
                    check_all_rules(v,call_back_on_finish)
                } else {
                    call_back_on_finish()
                }
            })
        })
    }

}