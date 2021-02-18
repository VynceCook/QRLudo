package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
import android.content.Intent
import android.net.Uri
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.TTSEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString
import fr.angers.univ.qrludo.utils.FileDownloader
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

/**
 * Load a user program (set of rules) in the CoreEngine. The user program
 * changes the behaviour of QRLudo to react (playing sound, launch speech
 * recognition or QR detection) to an external QR code (i.e. a QR code created
 * with another app, not with QRLudo)
 */
object QR_URL_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QR_URL", msg, level)
    }

    fun load_url(url_data: String) {
        if (MainApplication.Open_Link_In_Browser && (url_data.startsWith("http://") || url_data.startsWith(
                "https://"))) {
            // We open the link in an external web browser
            val browser_intent = Intent(Intent.ACTION_VIEW, Uri.parse(url_data))
            if (MainApplication.Main_Activity != null)
                MainApplication.Main_Activity!!.startActivity(browser_intent)
        } else {
            // We download the url and convert it to q QR code unique
            TTSEngine.text_to_file(context().getString(R.string.beginner_help_file_download_start), { media_file_name: String ->
                if (!MainApplication.Expert_Mode) {
                    MediaPlayerEngine.stop()
                    MediaPlayerEngine.play(
                        media_file_name, { _: String -> })
                }

                // We do the work in the callback to be sure that TTSEngine is IDLE
                FileDownloader().download_file(url_data,
                    MainApplication.get_media_files_path() + FileDownloader().encode_url(url_data) + ".txt",
                    { file_name: String ->
                        val qrcode_object : JSONObject = JSONObject()
                        qrcode_object.put("version", 1)
                        qrcode_object.put("id", 1)
                        qrcode_object.put("type", "unique")
                        qrcode_object.put("name", "Plain text")
                        val data_array_str : JSONArray = JSONArray()

                        val doc : Document = Jsoup.parse(File(file_name), "UTF-8")
                        val list_of_headings_h1 : MutableList<String> = mutableListOf()
                        val list_of_headings_h2 : MutableList<String> = mutableListOf()

                        for (heading in doc.body().getElementsByTag("h1"))
                            list_of_headings_h1.add(heading.text())
                        for (heading in doc.body().getElementsByTag("h2"))
                            list_of_headings_h2.add(heading.text())

                        var str_body = doc.text()
                        while (list_of_headings_h1.isNotEmpty() || list_of_headings_h2.isNotEmpty()) {
                            var str_section : String
                            val i1 = if (list_of_headings_h1.isEmpty()) -1 else str_body.indexOf(list_of_headings_h1.first())
                            val i2 = if (list_of_headings_h2.isEmpty()) -1 else str_body.indexOf(list_of_headings_h2.first())
                            if ((i2 == -1) || ((i1 != - 1) && (i1 < i2))) {
                                list_of_headings_h1.removeFirst()
                                str_section = str_body.substring(0,i1)
                                str_body = str_body.substring(i1)
                            } else {
                                list_of_headings_h2.removeFirst()
                                str_section = str_body.substring(0,i2)
                                str_body = str_body.substring(i2)
                            }
                            if (str_section.isNotEmpty())
                                data_array_str.put(str_section)
                        }
                        if (data_array_str.length() == 0)
                            data_array_str.put(context().getString(R.string.filedownload_file_unknown_error))
                        qrcode_object.put("data", data_array_str)

                        // Insert the new QR code in the Core Engine
                        CoreEngine.insert(EngineVarString("QR_code", qrcode_object.toString()))
                    })
            })
        }
    }
}