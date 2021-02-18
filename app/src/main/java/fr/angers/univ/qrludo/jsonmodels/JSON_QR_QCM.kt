package fr.angers.univ.qrludo.jsonmodels

import com.google.gson.annotations.SerializedName

/**
 * Describes the additional JSON fields of a "Vocal QCM"
 */
class JSON_QR_QCM : JSON_QR() {
    @SerializedName("text_bonne_reponse")
    var text_bonne_reponse : String? = null

    @SerializedName("text_mauvaise_reponse")
    var text_mauvaise_reponse : String? = null

    @SerializedName("lettreReponseVocale")
    var lettreReponseVocale : Boolean? = null
}