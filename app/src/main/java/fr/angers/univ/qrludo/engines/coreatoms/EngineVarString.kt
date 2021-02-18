package fr.angers.univ.qrludo.engines.coreatoms

/**
 * EngineVarString is a CoreEngine variable which represents
 * a string. When equality is testing with a EngineVarRegex, a matching
 * operator is used to ensure that the Regex matches the current string.
 */
class EngineVarString(name : String, value : String) : EngineVar {
    override val _name: String = name
    val _value : String = value

    override fun to_string(): String {
        return "String:$_name($_value)"
    }

    override fun equals(other: Any?): Boolean {
        if (other is EngineVarString) {
            return ((_name == other._name) && (_value == other._value))
        } else if (other is EngineVarRegex) {
            return ((_name == other._name) && (Regex(other._value, RegexOption.IGNORE_CASE).containsMatchIn(_value)))
        } else {
            return false
        }
    }

    override fun value_to_string(): String {
        return _value
    }
}