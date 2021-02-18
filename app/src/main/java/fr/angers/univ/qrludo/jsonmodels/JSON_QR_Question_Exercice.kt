package fr.angers.univ.qrludo.jsonmodels

import android.content.res.TypedArray
import com.google.gson.annotations.SerializedName

/**
 * Describes the additional JSON fields of a "Question Exercice QR code"
 */
class JSON_QR_Question_Exercice : JSON_QR() {
    @SerializedName("nb_min_reponses")
    var nb_min_reponses : Int? = null

    @SerializedName("text_bonne_reponse")
    var text_bonne_reponse : String? = null

    @SerializedName("text_mauvaise_reponse")
    var text_mauvaise_reponse : String? = null

    @SerializedName("data")
    var data : Array<String>? = null
}