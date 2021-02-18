package fr.angers.univ.qrludo.engines.coreatoms

/**
 * EngineVarRegex is a CoreEngine variable which represents
 * a regular expression. When equality is testing with a EngineVarString, a matching
 * operator is used to ensure that the Regex matches the given string.
 */
class EngineVarRegex(name : String, value : String) : EngineVar {
    override val _name: String = name
    val _value : String = value

    override fun to_string(): String {
        return "Regex:$_name[$_value]"
    }

    override fun equals(other: Any?): Boolean {
        if (other is EngineVarRegex) {
            return ((_name == other._name) && (_value == other._value))
        } else if (other is EngineVarString) {
            return ((_name == other._name) && (Regex(_value, RegexOption.IGNORE_CASE).containsMatchIn(other._value)))
        } else {
            return false
        }
    }

    override fun value_to_string(): String {
        return _value
    }
}