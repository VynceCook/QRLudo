package fr.angers.univ.qrludo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import fr.angers.univ.qrludo.MainActivity
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.QRDetectorEngine
import java.io.File

/**
 * Application class used to get some global variables and application
 * context from anywhere in the QRLudo application.
 * Is is based on a companion object linked with the class
 */
@SuppressLint("StaticFieldLeak")
class MainApplication : Application() {
    init {
        instance = this
    }

    // The companion object acts like a singleton (it is a global single object instance)
    companion object{
        private var instance : MainApplication? = null

        // Global values
        var QRCode_Format : Int = 4 // To update each time we change the format the app recognize
        var Main_Activity : MainActivity? = null
        var Camera_View : SurfaceView? = null

        var Media_Files_Path : String? = null /// Path for all media files

        var Shared_Pref_Name : String = "qrludo_prefs" /// Name of shared prefernces
        var Play_Speed : Float = 1.0F /// MediaPlayer default speed playing
        var Expert_Mode : Boolean = false /// True if we want disable all vocal help message
        var Open_Link_In_Browser : Boolean = true /// True if we want to open usual QR code link in browser

        var Log_To_File : Boolean = true /// True if we want to log events to log file
        val Log_File_Name : String = "qrludo_log.txt" /// Log file name

        val Pending_Download_Files : MutableList< String > = mutableListOf() /// List of files that are downloading in background

        fun application_context() : Context {
            return instance!!.applicationContext
        }

        // Attach the MainActivity to the MainApplication object in order
        // to access it later
        fun attach(activity: MainActivity, surface_view: SurfaceView) {
            Main_Activity = activity
            Camera_View = surface_view
        }

        // Detach the MainActivity from the MainApplication before the activity will be destroyed
        fun detach() {
            Main_Activity = null
            Camera_View = null
        }

        // Returns the path of the QRLudo storage directory (used to store log and media files)
        fun get_media_files_path() : String? {
            if (Media_Files_Path.isNullOrEmpty()) {
                Logger.log("MediaFilesDir", application_context().getString(R.string.media_diretory_not_found),Logger.DEBUG_LEVEL.CONSOLE_ONLY)
                return null
            }
            val media_path_file = File(Media_Files_Path!!)
            if (!media_path_file.exists() || !media_path_file.isDirectory()) {
                Logger.log("MediaFilesDir", application_context().getString(R.string.media_diretory_not_found),Logger.DEBUG_LEVEL.CONSOLE_ONLY)
                return null
            }
            return Media_Files_Path!!
        }

        // Load the shared preferences in MainApplication object
        fun load_preferences() {
            var settings = application_context().getSharedPreferences(Shared_Pref_Name, MODE_PRIVATE)

            if (settings != null)
            {
                Media_Files_Path = settings.getString("media_files_path", application_context().getExternalFilesDir(null)?.absolutePath.toString() + "/qrludo/")
                Play_Speed = settings.getFloat("play_speed", 1.0F)
                Expert_Mode = settings.getBoolean("expert_mode", false)
                Log_To_File = settings.getBoolean("log_to_file", true)
                Open_Link_In_Browser =  settings.getBoolean("open_link_in_browser", true)
            }
        }
    }
}