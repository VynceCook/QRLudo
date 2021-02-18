package fr.angers.univ.qrludo.engines.coreatoms

/**
 * EngineVarFloat is a CoreEngine variable which represents
 * a floating point value.
 */
class EngineVarFloat(name : String, value : Float) : EngineVar{
    override val _name: String = name
    val _value : Float = value

    override fun to_string(): String {
        return "Float:$_name($_value)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is EngineVarFloat)
            return false
        return ((_name == other._name) && (_value == other._value))
    }

    override fun value_to_string(): String {
        return "$_value"
    }
}