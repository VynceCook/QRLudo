package fr.angers.univ.qrludo.jsonmodels

import com.google.gson.annotations.SerializedName

/**
 * Describes the additional JSON fields of a "Vocal Open Question"
 */
class JSON_QR_Open_Question : JSON_QR() {
    @SerializedName("text_bonne_reponse")
    var text_bonne_reponse : String? = null

    @SerializedName("text_mauvaise_reponse")
    var text_mauvaise_reponse : String? = null

    @SerializedName("data")
    var data : Array<String>? = null
}