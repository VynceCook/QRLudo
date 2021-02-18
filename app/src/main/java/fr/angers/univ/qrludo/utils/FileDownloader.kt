package fr.angers.univ.qrludo.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.TTSEngine
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Class used to download any file from Internet (mostly sound files)
 */
class FileDownloader {

    private fun context() : Context
    {
        return MainApplication.application_context()
    }

    private fun logger(msg : String, level : Logger.DEBUG_LEVEL)
    {
        Logger.log("FileDownloader", msg, level)
    }

    // Encode an url in Base64 in order to get a string without any unconventional
    // symbol
    fun encode_url(_url : String): String {
        var url = _url
        // We resize the size of the string to get a not too long file name
        if(url.contains(".com"))
            url = url.substring(url.indexOf(".com"))
        else if(url.contains(".fr"))
            url = url.substring(url.indexOf(".fr"))
        url = url.replace(".com","")

        val encoded_url = Base64.encodeToString(url.toByteArray(),Base64.DEFAULT);

        if(url.length > 100)
            return encoded_url.substring(0, 10).replace("=", "").replace("/", "_")
        else
            return encoded_url.replace("=","").replace("/","_")
    }

    // Download the file in a separate thread and call a callback when download is finished
    // url, the given url to use for downloading the file
    // dst_file_name, the file name which will be used to store the file on the device
    // call_after_complete, callback called after the download of the file
    private fun do_download_file(url : String, dst_file_name : String, call_after_complete: (String) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            /*
            * Your task will be executed here
            * you can execute anything here that
            * you cannot execute in UI thread
            * for example a network operation
            * This is a background thread and you cannot
            * access view elements here
            *
            * its like doInBackground()
            * */
            val real_url = URL(url)
            var httpClient : HttpURLConnection? = null
            try {
                httpClient = real_url.openConnection() as HttpURLConnection
                if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                    logger(context().getString(R.string.filedownload_start_download) + url + "(" + dst_file_name + ")",
                        Logger.DEBUG_LEVEL.INFO)
                    // download the file
                    val input_stream = httpClient.inputStream
                    val output_stream = FileOutputStream(dst_file_name)

                    val data = ByteArray(4096)
                    var count : Int
                    count = input_stream.read(data)
                    while (count != -1) {
                        output_stream.write(data, 0, count);
                        count = input_stream.read(data)
                    }

                    output_stream.close();
                } else {
                    logger(context().getString(R.string.filedownload_file_error) + dst_file_name + " ERROR: " + httpClient.responseCode,
                        Logger.DEBUG_LEVEL.ERROR)
                    val f = File(dst_file_name)
                    f.createNewFile()
                }
            } catch (e: Exception) {
                logger(context().getString(R.string.filedownload_file_unknown_error) + dst_file_name,
                    Logger.DEBUG_LEVEL.ERROR)
                val f = File(dst_file_name)
                f.createNewFile()
                e.printStackTrace()
            } finally {
                httpClient?.disconnect()
            }
            handler.post {
                /*
                * You can perform any operation that
                * requires UI Thread here.
                *
                * its like onPostExecute()
                * */
                logger(context().getString(R.string.filedownload_file_downloaded) + dst_file_name,
                    Logger.DEBUG_LEVEL.INFO)
                call_after_complete(dst_file_name)
            }
        }
    }

    // Public function called to download a file
    // url, the given url to use for downloading the file
    // dst_file_name, the file name which will be used to store the file on the device
    // call_after_complete, callback called after the download of the file
    fun download_file(url : String, dst_file_name : String, call_after_complete: (String) -> Unit) {
        val file = File(dst_file_name)
        file.setReadable(true, false)

        // File already exists, skip download part
        if (file.exists()) {
            logger(context().getString(R.string.filedownload_file_exists) + dst_file_name,
                Logger.DEBUG_LEVEL.VERBOSE)
            call_after_complete(dst_file_name)
            return
        }
        do_download_file(url, dst_file_name, call_after_complete)
    }

    // Download multiple files at once (parallel mode). The callback is called once
    // all files have been downloaded.
    // items, a list of pair(url, dst_dile_name) to download
    // dst_file_name, the file name which will be used to store the file on the device
    // call_after_complete, callback called after the download of the file
    // Be careful! Do not call this function another time just after the previous call. Indeed, the
    // list of pending files is global, so it can be populated by any function call
    fun download_files(items : List< Pair<String, String> >, call_after_complete: () -> Unit) {
        val files_to_dowload : MutableList< Pair<String, String> > = mutableListOf()

        // Build the list of files to download by removing the already
        // existing files.
        // The pending files is stored in the MainApplication object.
        for (item in items) {
            val dst_file_name = item.second

            val file = File(dst_file_name)
            file.setReadable(true, false)

            // File already exists, skip download part
            if (file.exists()) {
                logger(context().getString(R.string.filedownload_file_exists) + dst_file_name,
                    Logger.DEBUG_LEVEL.VERBOSE)
            } else {
                MainApplication.Pending_Download_Files.add(dst_file_name)
                files_to_dowload.add(item)

            }
        }

        if (files_to_dowload.isEmpty()) {
            call_after_complete()
        } else {
            for (item in files_to_dowload) {
                val url = item.first
                val dst_file_name = item.second

                // Call the download function for each file
                do_download_file(url, dst_file_name, { _ : String ->
                    MainApplication.Pending_Download_Files.remove(dst_file_name)
                    if (MainApplication.Pending_Download_Files.isEmpty())
                        call_after_complete()
                })
            }
        }
    }
}