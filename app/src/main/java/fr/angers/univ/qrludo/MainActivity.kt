package fr.angers.univ.qrludo

// TODO : Because serious game answers don't have a QR Code format, we have to leave it that way, but it must be fix as soon as possible
// TODO : ReponseSeriousGame should be QR code unique et not specific QR
// TODO : Music in SeriousGameScenario, QR_Vocal_QCM and QCM_Vocal_OQ are hard coded and can be tested only beacause of starting with http:// or https://

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import fr.angers.univ.qrludo.engines.*
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarBool
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarInt
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString
import fr.angers.univ.qrludo.jsonmodels.program_loads.QR_Ludo_Program
import fr.angers.univ.qrludo.utils.*
import java.io.File
import kotlin.system.exitProcess

/**
 * Main activity of QRLudo.
 * Starts the global engine and wait for user gestures.
 */
class MainActivity : AppCompatActivity() {
    val REQUEST_CODE_SETTINGS : Int = 90
    val REQUEST_CODE_HELP : Int = 91

    private lateinit var _gesture_detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the QR detector engine
        val camera_view = findViewById<SurfaceView>(R.id.camera_view)
        MainApplication.attach(this, camera_view)
        QRDetectorEngine.init_engine()
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                CoreEngine.insert( EngineVarBool("_CORESYS_QR_initialized_self_timer", true))
            }
        }, 3000)


        // Check all needed permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtil.check_and_request_permissions(this,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA)
            ) {
                Logger.log("PermissionCheck",
                    getString(R.string.permissions_granted),
                    Logger.DEBUG_LEVEL.INFO)
                init_application()
            }
        }
    }

    // Release the engines when the activity is destroyed
    override fun onDestroy() {
        QRDetectorEngine.destroy_engine()
        MainApplication.detach()
        super.onDestroy()
    }

    // Init the application components
    private fun init_application() {
        // Load application preferences
        MainApplication.load_preferences()

        // Load QRLudo Macro program
        QR_Ludo_Program.load()

        // Check Media files directory
        val media_files_path = MainApplication.Media_Files_Path
        if (media_files_path == null)
        {
            Logger.log("MediaFilesDir",
                getString(R.string.media_diretory_not_found) + "(empty)",
                Logger.DEBUG_LEVEL.INFO)
            Toast.makeText(this, getString(R.string.media_diretory_not_found), Toast.LENGTH_LONG).show()
            exit_application(3000)
        } else {
            val media_files_dir = File(media_files_path)
            media_files_dir.setReadable(true, false)

            if (!media_files_dir.exists()) {
                Logger.log("MediaFilesDir",
                    getString(R.string.media_diretory_create) + "(" + media_files_dir.absolutePath + ")",
                    Logger.DEBUG_LEVEL.INFO)
                media_files_dir.mkdir()
            }
            // Exit if no media files directory found
            if (!media_files_dir.exists() || !media_files_dir.isDirectory()) {
                Logger.log("MediaFilesDir",
                    getString(R.string.media_diretory_not_found) + "(" + media_files_dir.absolutePath + ")",
                    Logger.DEBUG_LEVEL.INFO)
                Toast.makeText(this, getString(R.string.media_diretory_not_found), Toast.LENGTH_LONG).show()
                exit_application(3000)
            }
        }

        // Clear log file
        if (MainApplication.Log_To_File) {
            val log_file_path = MainApplication.get_media_files_path()
            if (log_file_path != null)
            {
                val log_file = File(log_file_path + MainApplication.Log_File_Name)
                log_file.setReadable(true, false)
                if (log_file.exists())
                    log_file.delete()
            }
        }

        // Init TTS Engine
        TTSEngine.init_engine()

        // Check sound level
        val audioManager : AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 4) {
            val vibrator : Vibrator = applicationContext.getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(3000,10))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(3000)
            }
            Toast.makeText(this, getString(R.string.sound_level_low_alert), Toast.LENGTH_LONG).show()
        }

        // Init Gesture dectector with all listener triggered on events
        _gesture_detector = GestureDetectorCompat(this,
            object : OnSwipeTouchListener() {
                override fun on_double_tap() {
                    Logger.log("SwipeGesture", "Double tap", Logger.DEBUG_LEVEL.VERBOSE)
                    MediaPlayerEngine.pause_or_resume()
                }

                override fun on_swipe_left() {
                    Logger.log("SwipeGesture", "Swipe left", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    CoreEngine.insert(EngineVarInt("seek_section", -2))
                }

                override fun on_swipe_right() {
                    Logger.log("SwipeGesture", "Swipe right", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    CoreEngine.insert(EngineVarInt("seek_section", 0))
                }

                override fun on_swipe_up() {
                    Logger.log("SwipeGesture", "Swipe up", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    MediaPlayerEngine.seek(-5)
                }

                override fun on_swipe_down() {
                    Logger.log("SwipeGesture", "Swipe down", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    CoreEngine.insert(EngineVarInt("seek_section", -1))
               }

                override fun on_double_swipe_left() {
                    Logger.log("SwipeGesture", "Double swipe left", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    CoreEngine.insert(EngineVarInt("seek_section", -1000))
                }

                override fun on_double_swipe_right() {
                    Logger.log("SwipeGesture", "Souble swipe right", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    CoreEngine.insert(EngineVarInt("seek_section", 1000))
                }

                override fun on_double_swipe_up() {
                    Logger.log("SwipeGesture", "Double swipe up", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    if (CoreEngine.is_backup_user_rules()) {
                        CoreEngine.clear_user_rules()
                        CoreEngine.clear_user_var_store()
                        if (MediaPlayerEngine.is_playing())
                            MediaPlayerEngine.stop()
                        if (SpeechRecognitionEngine.is_recording())
                            SpeechRecognitionEngine.cancel()
                        if (QRDetectorEngine.is_scanning())
                            QRDetectorEngine.cancel()
                        CoreEngine.clear_user_rules()
                        CoreEngine.restore_backup_user_rules()
                        CoreEngine.clear_backup_user_rules()
                        CoreEngine.insert(EngineVarBool("ask_for_restore_user_rules", true))
                    } else {
                        CoreEngine.insert(EngineVarBool("ask_for_backup_user_rules", true))
                    }
                }

                override fun on_double_swipe_down() {
                    Logger.log("SwipeGesture", "Double swipe down", Logger.DEBUG_LEVEL.VERBOSE)
                    clear_pretty_print()
                    CoreEngine.clear_user_rules()
                    CoreEngine.clear_user_var_store()
                    if (MediaPlayerEngine.is_playing())
                        MediaPlayerEngine.stop()
                    if (SpeechRecognitionEngine.is_recording())
                        SpeechRecognitionEngine.cancel()
                    if (QRDetectorEngine.is_scanning())
                        QRDetectorEngine.cancel()
                    CoreEngine.clear_user_var_store()
                    CoreEngine.insert(EngineVarBool("QR_start", true))
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grant_results: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grant_results)
        PermissionUtil.on_request_permissions_result(this, requestCode, permissions, grant_results,
            { ->
                Logger.log("PermissionCheck",
                    getString(R.string.permissions_granted),
                    Logger.DEBUG_LEVEL.DEBUG)
                Toast.makeText(this, getString(R.string.permissions_granted), Toast.LENGTH_SHORT)
                    .show()
                init_application()
            },
            { ->
                Logger.log("PermissionCheck",
                    getString(R.string.permissions_denied),
                    Logger.DEBUG_LEVEL.DEBUG)
                Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_LONG)
                    .show()
                exit_application(3000)
            }
        )
    }

    // Trigger the gesture detector on any user event
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return _gesture_detector.onTouchEvent(event)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                exit_application(0)
                true
            }
            R.id.action_help -> {
                val pick_option_intent = Intent(this, HelpActivity::class.java)
                startActivityForResult(pick_option_intent, REQUEST_CODE_HELP)
                true
            }
            R.id.action_settings -> {
                val pick_option_intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(pick_option_intent, REQUEST_CODE_SETTINGS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Make the app immersive
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // Hide menu bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    // Exit the app and close any process. It really exit with a delay
    // order to show a Toast message in order to give the user the reason
    // of the exit
    fun exit_application(delay_before_exit_msec : Long) {
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                if(Build.VERSION.SDK_INT>=16 && Build.VERSION.SDK_INT<21){
                    finishAffinity();
                    exitProcess(0)
                } else if(Build.VERSION.SDK_INT>=21){
                    finishAndRemoveTask();
                    exitProcess(0)
                }
            }
        }, delay_before_exit_msec)
    }

    // Update the text widget to show user some text messages (spoken texts, recognised texts, ...)
    fun pretty_print( msg : String) {
        val txt_pretty_print = findViewById<TextView>(R.id.txt_pretty_print)
        txt_pretty_print.setText(msg)
    }

    // Clear the text widget
    fun clear_pretty_print() {
        val txt_pretty_print = findViewById<TextView>(R.id.txt_pretty_print)
        txt_pretty_print.setText("")
    }
}