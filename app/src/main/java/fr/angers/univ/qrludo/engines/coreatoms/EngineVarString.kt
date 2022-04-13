package fr.angers.univ.qrludo.engines.coreatoms

import fr.angers.univ.qrludo.utils.Phoneme

/**
 * EngineVarString is a CoreEngine variable which represents
 * a string. When equality is testing with a EngineVarRegex, a matching
 * operator is used to ensure that the Regex matches the current string.
 */
class EngineVarString(name : String, value : String, with_phoneme : Boolean = false) : EngineVar {
    override val _name: String = name
    val _value : String = value
    val _with_phoneme : Boolean = with_phoneme

    override fun to_string(): String {
        return "String:$_name($_value)"
    }

    override fun equals(other: Any?): Boolean {
        val convert = { str: String -> if (_with_phoneme) Phoneme.convert(str) else Phoneme.identity(str) }

        if (other is EngineVarString) {
            return ((_name == other._name) && (convert(_value) == convert(other._value)))
        } else if (other is EngineVarRegex) {
            return ((_name == other._name) && (Regex(convert(other._value), RegexOption.IGNORE_CASE).containsMatchIn(convert(_value))))
        } else {
            return false
        }
    }

    override fun value_to_string(): String {
        return _value
    }
}