package fr.angers.univ.qrludo.engines.coreatoms.actions

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.coreatoms.*
import fr.angers.univ.qrludo.jsonmodels.JSON_QR
import fr.angers.univ.qrludo.jsonmodels.program_loads.*
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * Action that analyse a QRCode content.
 * Depending on the type of the QRCode a corresponding program is loaded.
 * The main goal of this action is to decompress a zip (base64) encoded QR code content
 * and deciding which type of program we have to load.
 */
class ActionAnalyseQRCode : EngineAction {
    override val _name: String = "QRAnalyse"

    override fun to_string(): String {
        return super.to_string()
    }

    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("ActionQRAnalyse", msg, level)
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        for (v in var_list) {
            if ((v._name == "QR_code") && (v is EngineVarString) && v.value_to_string().isNotEmpty()) {
                var data = v.value_to_string()
                logger(context().getString(R.string.action_qranalyse_new) + " : $data", Logger.DEBUG_LEVEL.VERBOSE)

                // Check is the data are base64 encoded
                if (data.matches(Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) ) {
                    // Début de la décompression
                    try {
                        data = decompress(data)
                        logger(context().getString(R.string.action_qranalyse_decompress) + " : $data",
                            Logger.DEBUG_LEVEL.VERBOSE)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        break
                    }
                }

                // If the data is not JSON so it is only text or link from a classical (external) QR code
                if (!data.startsWith("{")) {
                    if (data.startsWith("http://") || data.startsWith("https://")) {
                        QR_URL_Program.load_url(data)
                        break
                    } else {
                        // Plain text, we convert it to a QR Code Unique
                        val qrcode_object : JSONObject = JSONObject()
                        qrcode_object.put("version", 1)
                        qrcode_object.put("id", 1)
                        qrcode_object.put("type", "unique")
                        qrcode_object.put("name", "Plain text")
                        qrcode_object.put("data", JSONArray().put(data))
                        data = qrcode_object.toString()
                    }
                }

                val gson: Gson = GsonBuilder().create()
                val converted_json_object: JSON_QR = gson.fromJson(data, JSON_QR::class.java)

                // TODO : Because serious game answers don't have a QR Code format, we have to leave it that way, but it must be fix as soon as possible
                // if ((converted_json_object.version == null) || (converted_json_object.version!! > MainApplication.QRCode_Format)) {
                if ((converted_json_object.version != null) && (converted_json_object.version!! > MainApplication.QRCode_Format)) {
                    CoreEngine.insert(EngineVarInt("QR_code_error", 1), call_back_on_finish)
                    return
                }

                when (converted_json_object.type)
                {
                    "unique", "xl", "atomique" -> QR_Unique_Program.load_from_json(data) // xl and atomique seem deprecated, but still exist in old QR Codes
                    "ensemble" -> QR_Multiple_Program.load_from_json(data)
                    "question" -> QR_Exercice_Question_Program.load_from_json(data)
                    "ExerciceReconnaissanceVocaleQCM" -> QR_Vocal_QCM_Program.load_from_json(data)
                    "ExerciceReconnaissanceVocaleQuestionOuverte" -> QR_Vocal_OQ_Program.load_from_json(data)
                    "reponse" -> logger(context().getString(R.string.action_qranalyse_unique_unkown_type) + " : $data", Logger.DEBUG_LEVEL.VERBOSE)
                    "SeriousGameScenario" -> QR_Serious_Game_Program.load_from_json(data)
                    "ReponseSeriousGame" -> {
                        // Outside from a SeriousGame, we convert it to a QR Code Unique
                        val qrcode_object : JSONObject = JSONObject()
                        qrcode_object.put("version", 1)
                        qrcode_object.put("id", 1)
                        qrcode_object.put("type", "unique")
                        qrcode_object.put("name", "Plain text")
                        qrcode_object.put("data", JSONArray().put(converted_json_object.name))
                        QR_Unique_Program.load_from_json(qrcode_object.toString())
                    }
                }
            }
        }
        call_back_on_finish()
    }

    // Decompress zip encoded string
    private fun decompress(zip_string: String) : String {
        val decode = Base64.decode(zip_string, Base64.DEFAULT) // Decompress string in byte[]
        val ba_in_stream = ByteArrayInputStream(decode)
        val gzip_in_stream = GZIPInputStream(ba_in_stream)
        val br = BufferedReader(InputStreamReader(gzip_in_stream, "UTF-8"))
        val sb = StringBuilder()
        var line : String? = br.readLine()
        while (line != null) {
            sb.append(line)
            line = br.readLine()
        }
        br.close()
        gzip_in_stream.close()
        ba_in_stream.close()
        return sb.toString()
    }

}