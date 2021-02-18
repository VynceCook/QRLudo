package fr.angers.univ.qrludo.engines.coreatoms

/**
 * EngineVar is the interface used to create Typed Variables which
 * will be stored in the store of variables of the CoreEngine.
 * A EngineVar is characterized by a name.
 * Other members will be specify in the classes which implement
 * this interface.
 */
interface EngineVar {
    val _name : String

    // Returns a string representation of a var
    fun to_string() : String {
        return _name
    }

    // Returns a string representation of the value of the var
    fun value_to_string() : String
}