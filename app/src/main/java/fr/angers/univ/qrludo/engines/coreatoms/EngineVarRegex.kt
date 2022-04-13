package fr.angers.univ.qrludo.engines.coreatoms

import fr.angers.univ.qrludo.utils.Phoneme

/**
 * EngineVarRegex is a CoreEngine variable which represents
 * a regular expression. When equality is testing with a EngineVarString, a matching
 * operator is used to ensure that the Regex matches the given string.
 */
class EngineVarRegex(name : String, value : String, with_phoneme : Boolean = false) : EngineVar {
    override val _name: String = name
    var _value : String = value
    val _with_phoneme : Boolean = with_phoneme

    init {
        // TODO: Fix for bug in QRLudo Generator about Regex
        if (_value.startsWith("(^((?!")) {
            _value = _value.replace(").)*$((?!",")|.*(")
            _value = _value.replace("(^((?!","^(?!.*(")
            _value = _value.replace(").)*$)",")).*$")
        }
    }

    override fun to_string(): String {
        return "Regex:$_name[$_value]"
    }

    override fun equals(other: Any?): Boolean {
        val convert = { str: String -> if (_with_phoneme) Phoneme.convert(str) else Phoneme.identity(str) }

        if (other is EngineVarRegex) {
            return ((_name == other._name) && (convert(_value) == convert(other._value)))
        } else if (other is EngineVarString) {
            return ((_name == other._name) && (Regex(convert(_value), RegexOption.IGNORE_CASE).containsMatchIn(convert(other._value))))
        } else {
            return false
        }
    }

    override fun value_to_string(): String {
        return _value
    }
}