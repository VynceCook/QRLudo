package fr.angers.univ.qrludo.utils

import kotlin.math.min


/**
 * Phoneme singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * The phoneme object is used to process string and replace some sequence
 * of caracters by the corresponding phoneme.
 */
object Phoneme {
    /// List of phonemes and their keys
    var phoneme_map : HashMap<String, String> = hashMapOf(
        "a" to "<a>",
        "à" to "<a>",
        "ah" to "<a>",
        "ou" to "<u>",
        "où" to "<u>",
        "oû" to "<u>",
        "1" to "<1>",
        "un" to "<1>",
        "2" to "<2>",
        "deux" to "<2>",
        "3" to "<3>",
        "trois" to "<3>",
        "4" to "<4>",
        "quatre" to "<4>",
        "5" to "<5>",
        "cinq" to "<5>",
        "6" to "<6>",
        "six" to "<6>",
        "7" to "<7>",
        "sept" to "<7>",
        "8" to "<8>",
        "huit" to "<8>",
        "9" to "<9>",
        "neuf" to "<9>",
        "10" to "<10>",
        "dix" to "<10>"
    )

    /// Max length of a phoneme key
    var phoneme_max_key_length = 0

    init {
        phoneme_map.forEach { (key, _) -> if (key.length > phoneme_max_key_length) phoneme_max_key_length = key.length }
    }

    // Fake function used to get a better code in EngineVarXXXX
    fun identity(str_in : String) : String {
        return str_in
    }

        // Analyse the str_in string and replace some sequence of character by the corresponding phoneme
    fun convert(str_in : String) : String {
        var str = str_in.toLowerCase()
        var i = 0
        while (i < str.length)
        {
            // Go over existing phoneme
            if (str[i] == '<')
            {
                while ((i < str.length) and (str[i] != '>'))
                    ++i
                ++i
            }
            if (str[i] == '[')
            {
                while ((i < str.length) and (str[i] != ']'))
                    ++i
                ++i
            }
            var key = ""
            var is_phoneme = false
            var j = min(phoneme_max_key_length,str.length-i);
            // Search for largest phoneme
            while (!(is_phoneme) and (j != 0))
            {
                key = str.substring(i,i+j)
                is_phoneme = phoneme_map.containsKey(key)
                --j
            }
            // Update string on new phoneme
            if (is_phoneme) {
                val str_prefix = str.substring(0,i)
                val str_suffix = str.substring(i+key.length)
                val value = phoneme_map[key]
                str = str_prefix + value + str_suffix
                i += value!!.length
            } else
                ++i
        }
        Logger.log("PhonemeConvert", "Phoneme conversion from $str_in to $str", Logger.DEBUG_LEVEL.VERBOSE)
        return str
    }
}