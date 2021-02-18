package fr.angers.univ.qrludo.jsonmodels

import com.google.gson.annotations.SerializedName

/**
 * Describes the additional JSON fields of a "Serious Game" QR code
 */
class JSON_QR_Serious_Game : JSON_QR() {
    @SerializedName("introduction")
    var introduction : String? = null

    @SerializedName("fin")
    var fin : String? = null
}