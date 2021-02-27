package fr.angers.univ.qrludo.utils

import android.content.Context
import android.util.Log
import fr.angers.univ.qrludo.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Logger singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * The logger object is used to log messages at the console
 * and in a file for debugging purposes.
 */
object Logger {
    /// Debugging message level
    enum class DEBUG_LEVEL {
        CONSOLE_ONLY,
        EXTRA_VERBOSE,
        VERBOSE,
        DEBUG,
        INFO,
        ERROR
    }

    private fun context() : Context
    {
        return MainApplication.application_context()
    }

    // Log the message
    // part_name, it is the name of the QRLudo part (MediaPlayerEngine, CoreEngine, etc ...) to distinguishes messages
    // msg, msg is the message to log
    // level, level is the debugging level
    fun log(part_name: String, msg: String, level: DEBUG_LEVEL)
    {
        val thread_id = android.os.Process.myTid()
        when (level) {
            DEBUG_LEVEL.EXTRA_VERBOSE -> Unit
            DEBUG_LEVEL.CONSOLE_ONLY -> Log.d(context().getString(R.string.qrludo_tag_name), "$part_name #$thread_id : $msg")
            DEBUG_LEVEL.VERBOSE -> Log.v(context().getString(R.string.qrludo_tag_name), "$part_name #$thread_id : $msg")
            DEBUG_LEVEL.DEBUG -> Log.d(context().getString(R.string.qrludo_tag_name), "$part_name #$thread_id : $msg")
            DEBUG_LEVEL.INFO -> Log.i(context().getString(R.string.qrludo_tag_name), "$part_name #$thread_id : $msg")
            DEBUG_LEVEL.ERROR -> Log.d(context().getString(R.string.qrludo_tag_name), "$part_name #$thread_id : $msg")
        }


        if ((level != DEBUG_LEVEL.CONSOLE_ONLY) && MainApplication.Log_To_File)
        {
            // Check if path exists
            val log_path : String? = MainApplication.get_media_files_path()
            if (log_path == null)
                return

            // Check if the log file exists
            val log_file = File(log_path + MainApplication.Log_File_Name)
            log_file.setReadable(true,false)

            if (log_file.exists() && !log_file.canWrite())
                return

            val stream = if (log_file.length() > (5 * 1024 * 1024))
                FileOutputStream(log_file,false)
            else
                FileOutputStream(log_file,true)
            try {
                val s = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                val cur_time: String = s.format(Date())
                stream.write("$cur_time |${level.name}| $part_name #$thread_id : $msg\n".toByteArray())
            } finally {
                stream.close()
            }
        }
    }
}