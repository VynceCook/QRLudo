package fr.angers.univ.qrludo.jsonmodels

import com.google.gson.annotations.SerializedName

/**
 * Describes the JSON fields of any QR code handled by QRLudo
 */
open class JSON_QR {
    @SerializedName("version")
    var version : Int? = null

    @SerializedName("id")
    var id : String? = null

    @SerializedName("type")
    var type : String? = null

    @SerializedName("name")
    var name : String? = null
}