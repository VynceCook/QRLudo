package fr.angers.univ.qrludo.engines.coreatoms

/**
 * EngineVarBool is a CoreEngine variable which can be either
 * True or False.
 */
class EngineVarBool(name : String, value : Boolean) : EngineVar {
    override val _name: String = name
    val _value : Boolean = value

    override fun to_string(): String {
        return "Bool:$_name($_value)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is EngineVarBool)
            return false
        return ((_name == other._name) && (_value == other._value))
    }

    override fun value_to_string(): String {
        return "$_value"
    }
}