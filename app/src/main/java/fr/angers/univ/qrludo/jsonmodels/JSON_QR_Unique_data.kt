package fr.angers.univ.qrludo.jsonmodels

import com.google.gson.annotations.SerializedName

/**
 * Describes the additional JSON fields of a "Unique" QR code.
 * The most used!
 */
class JSON_QR_Unique_data {

    @SerializedName("type")
    var type : String? = null

    @SerializedName("name")
    var name : String? = null

    @SerializedName("url")
    var url : String? = null
}