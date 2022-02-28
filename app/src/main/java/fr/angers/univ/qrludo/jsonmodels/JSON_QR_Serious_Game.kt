package fr.angers.univ.qrludo.jsonmodels

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

/**
 * Describes the additional JSON fields of a "Serious Game" QR code
 */
class JSON_QR_Serious_Game : JSON_QR() {
    @SerializedName("text_nodes")
    var label_nodes : JsonArray? = null

    @SerializedName("question_nodes")
    var question_nodes : JsonArray? = null
}