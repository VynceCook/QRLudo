package fr.angers.univ.qrludo.engines.coreatoms

/**
 * EngineVarInt is a CoreEngine variable which represents
 * an integer value.
 */
class EngineVarInt(name : String, value : Int) : EngineVar {
    override val _name: String = name
    val _value : Int = value

    override fun to_string(): String {
        return "Int:$_name($_value)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is EngineVarInt)
            return false
        return ((_name == other._name) && (_value == other._value))
    }

    override fun value_to_string(): String {
        return "$_value"
    }
}