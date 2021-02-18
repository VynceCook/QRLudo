package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.TTSEngine
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_Unique_data
import fr.angers.univ.qrludo.utils.FileDownloader
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Load a user program (set of rules) in the CoreEngine. The user program
 * changes the behaviour of QRLudo to react (playing sound, launch speech
 * recognition or QR detection) to a QR code type "Multiple" or "Ensemble".
 */
object QR_Multiple_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QRMultiple", msg, level)
    }

    fun load_from_json(data: String) {
        logger(context().getString(R.string.action_multiple_qr_found), Logger.DEBUG_LEVEL.INFO)
        val gson: Gson = GsonBuilder().create()

        val data_json_object = gson.fromJson(data, JsonObject::class.java)
        val data_json_array = data_json_object.getAsJsonArray("data")
        if (data_json_array != null) {
            // Clear all previous user rules
            CoreEngine.clear_user_rules()

            val files_to_download: MutableList<Pair<String, String>> = mutableListOf()
            val texts_to_synthetize: MutableList<String> = mutableListOf()
            // For each section of the data array of the QR code unique
            // A section can be either a "text" either a "music" file
            for (json_qrcode in data_json_array) {
                val qrcode_object = gson.fromJson(json_qrcode, JsonObject::class.java).asJsonObject
                val qrcode_data_object = qrcode_object.getAsJsonObject("qrcode")
                val qrcode_data_json_array = qrcode_data_object.getAsJsonArray("data")

                if (qrcode_data_json_array != null) {
                    for (json_qrcode_section in qrcode_data_json_array) {
                        try {
                            // The underlying section is a "text"
                            texts_to_synthetize.add(json_qrcode_section.toString())
                        } catch (e: Exception) {
                            val json_qrcode_section_object: JSON_QR_Unique_data =
                            gson.fromJson(json_qrcode_section, JSON_QR_Unique_data::class.java)
                            if ((json_qrcode_section_object.url != null) && (json_qrcode_section_object.type == "music")) {
                                // The underlying section is a "music"
                                if (json_qrcode_section_object.name.isNullOrEmpty())
                                    files_to_download.add( Pair(json_qrcode_section_object.url!!, MainApplication.Media_Files_Path + FileDownloader().encode_url(json_qrcode_section_object.url!!)) )
                                else
                                    files_to_download.add( Pair(json_qrcode_section_object.url!!, MainApplication.Media_Files_Path + json_qrcode_section_object.name!!) )
                            } else {
                                // We don't know this section type
                                logger(context().getString(R.string.action_qranalyse_unique_unkown_section_type) + " : " + json_qrcode_section_object.type,
                                    Logger.DEBUG_LEVEL.ERROR)
                            }
                        }
                    }
                }
            }

            if (files_to_download.isEmpty() && texts_to_synthetize.isEmpty())
                return

            TTSEngine.text_to_file(context().getString(R.string.beginner_help_file_download_start), { file_name: String ->
                // Speak an advertising message
                MediaPlayerEngine.stop()
                MediaPlayerEngine.play(
                    file_name,
                    { _ : String -> })

                // Download all files and start synthetising the remaining ones
                logger(context().getString(R.string.action_multiple_launch_downloads), Logger.DEBUG_LEVEL.INFO)
                FileDownloader().download_files(files_to_download, { ->
                    TTSEngine.texts_to_files(texts_to_synthetize, { ->
                        // Speak an ending advertising message
                        TTSEngine.text_to_file(context().getString(R.string.beginner_help_file_download_end), { file_name: String ->
                            MediaPlayerEngine.stop()
                            MediaPlayerEngine.play(
                                file_name, { _: String -> })
                        })
                    })
                })
            })
        }
    }
}