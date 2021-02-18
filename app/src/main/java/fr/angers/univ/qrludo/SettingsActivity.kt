package fr.angers.univ.qrludo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import java.io.File
import java.lang.Exception

/**
 * Settings activity
 *
 * Used to show setting widgets and show summary and handle actions
 * on setting changes
 */
class SettingsActivity : AppCompatActivity() {
    val PICK_REQUEST_CODE : Int = 80

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL)
    {
        Logger.log("Settings", msg, level)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val intent = Intent()
        if (parent == null) {
            setResult(RESULT_OK, intent)
        } else {
            parent.setResult(RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    // Find the full path of the given p_path. It replaces some parts by
    // usual path found on device (/storage, /emulated/0, ...)
    private fun find_full_path(p_path: String): String {
        // This functions source code comes from https://github.com/chetanborase/TreeUritoAbsolutePath
        var path = p_path.substring(5)
        var index = 0
        val result = StringBuilder("/storage")
        var i = 0
        while (i < path.length) {
            if (path[i] != ':') {
                result.append(path[i])
            } else {
                index = ++i
                result.append('/')
                break
            }
            ++i
        }
        for (j in index until path.length) {
            result.append(path[j])
        }
        var actualResult =
            if (result.substring(9, 16).equals("primary", ignoreCase = true)) {
                result.substring(0, 8) + "/emulated/0/" + result.substring(17)
            } else {
                result.toString()
            }
        return actualResult
    }

    // The onActivityResult is triggered when the user picked up an newer directory to
    // store the QRLudo media files.
    // It checks the directory and stores it in the shared preferences.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                var tree_uri: Uri? = data?.getData()
                if (tree_uri != null) {
                    var path_str : String = find_full_path(tree_uri.path.toString()) + "/"
                    logger(getString(R.string.file_directory_picker_done) + " : " + path_str,
                        Logger.DEBUG_LEVEL.INFO);
                    val dst_dir = File(path_str)
                    val test_file = File(path_str + "test_create.txt")
                    test_file.setReadable(true, false)
                    try {
                        test_file.createNewFile()
                    }  catch (e : Exception) {
                        logger(getString(R.string.media_directory_cannot_use) + " : " + path_str,
                            Logger.DEBUG_LEVEL.INFO);
                    }

                    if (dst_dir.exists() && dst_dir.isDirectory && test_file.exists()) {
                        test_file.delete()
                        MainApplication.Media_Files_Path = path_str
                        val settings =
                            getSharedPreferences(MainApplication.Shared_Pref_Name,
                                MODE_PRIVATE)?.edit()
                        settings?.putString("media_files_path", path_str)
                        settings?.apply()

                        logger(getString(R.string.media_directory_moved) + " : " + path_str,
                            Logger.DEBUG_LEVEL.INFO);
                    }
                }
            }
        }
    }

    /**
     * The Fragment that implement all settings
     */
    class SettingsFragment : PreferenceFragmentCompat() {
        val PICK_REQUEST_CODE : Int = 80

        private fun logger(msg: String, level: Logger.DEBUG_LEVEL)
        {
            Logger.log("Settings", msg, level)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Change expert mode switch
            val pref_expert_mode: TwoStatePreference? = findPreference("pref_expert_mode")
            pref_expert_mode?.setChecked(MainApplication.Expert_Mode)
            pref_expert_mode?.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    logger(MainApplication.application_context()
                        .getString(R.string.settings_expert_mode_changed) + " : " + newValue.toString(),
                        Logger.DEBUG_LEVEL.INFO)
                    MainApplication.Expert_Mode = newValue as Boolean
                    val settings =
                        activity?.getSharedPreferences(MainApplication.Shared_Pref_Name,
                            MODE_PRIVATE)?.edit()
                    settings?.putBoolean("exper_mode", newValue)
                    settings?.apply()
                    return true
                }
            })

            // Change log_to_file switch
            val pref_log_to_file: TwoStatePreference? = findPreference("pref_log_to_file")
            pref_log_to_file?.setChecked(MainApplication.Log_To_File)
            pref_log_to_file?.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    logger(MainApplication.application_context()
                        .getString(R.string.settings_log_to_file_changed) + " : " + newValue.toString(),
                        Logger.DEBUG_LEVEL.INFO)
                    MainApplication.Log_To_File = newValue as Boolean
                    val settings =
                        activity?.getSharedPreferences(MainApplication.Shared_Pref_Name,
                            MODE_PRIVATE)?.edit()
                    settings?.putBoolean("log_to_file", newValue)
                    settings?.apply()
                    return true
                }
            })

            // Change open_external_link switch
            val pref_open_external_link: TwoStatePreference? = findPreference("pref_open_external_link")
            pref_open_external_link?.setChecked(MainApplication.Open_Link_In_Browser)
            pref_open_external_link?.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    logger(MainApplication.application_context()
                        .getString(R.string.settings_open_external_link_changed) + " : " + newValue.toString(),
                        Logger.DEBUG_LEVEL.INFO)
                    MainApplication.Open_Link_In_Browser = newValue as Boolean
                    val settings =
                        activity?.getSharedPreferences(MainApplication.Shared_Pref_Name,
                            MODE_PRIVATE)?.edit()
                    settings?.putBoolean("open_link_in_browser", newValue)
                    settings?.apply()
                    return true
                }
            })

            // Change the speed of the MediaPlayer
            var local_play_speed = MainApplication.Play_Speed
            val pref_speed: Preference? = findPreference("pref_mediaplayer_speed")
            pref_speed?.summary =
                getString(R.string.pref_summary_play_speed) + " : " + local_play_speed
            pref_speed?.setOnPreferenceClickListener(object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {

                    val sliderView = View.inflate(activity, R.layout.slide_preference, null)
                    val slider = sliderView.findViewById<View>(R.id.slider) as SeekBar
                    //val sliderValue: Double = (local_play_speed - 0.8) * 5
                    val slider_off_set = slider.max / 2 - 2
                    slider.progress = (slider_off_set + (local_play_speed - 1) * 5).toInt()

                    slider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            seekBar.progress = progress
                            local_play_speed = 1 + (progress - slider_off_set).toFloat() / 5
                            pref_speed.summary =
                                getString(R.string.pref_summary_play_speed) + " : " + local_play_speed
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    })

                    AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setTitle(activity?.getString(R.string.alert_dialog_play_speed_title))
                        .setView(sliderView)
                        .setPositiveButton(android.R.string.ok,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    logger(MainApplication.application_context()
                                        .getString(R.string.settings_play_speed_pref_changed) + " : " + local_play_speed,
                                        Logger.DEBUG_LEVEL.INFO)
                                    MainApplication.Play_Speed = local_play_speed
                                    val settings =
                                        activity?.getSharedPreferences(MainApplication.Shared_Pref_Name,
                                            MODE_PRIVATE)?.edit()
                                    settings?.putFloat("play_speed", local_play_speed)
                                    settings?.apply()
                                }
                            })
                        .setNegativeButton(android.R.string.cancel,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    slider.progress =
                                        (slider_off_set + (MainApplication.Play_Speed - 1) * 5).toInt()
                                    pref_speed.summary =
                                        getString(R.string.pref_summary_play_speed) + " : " + MainApplication.Play_Speed
                                    dialog?.cancel()
                                }
                            })
                        .create()
                        .show()
                    return true
                }
            })

            // Change media files directory
            val pref_change_media_files_dir: Preference? = findPreference("pref_change_media_files_dir")
            pref_change_media_files_dir?.setSummary("""${getString(R.string.pref_summary_change_media_files_dir)} : ${MainApplication.Media_Files_Path}""")
            pref_change_media_files_dir?.setOnPreferenceClickListener(object :
                Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    activity?.startActivityForResult(intent, PICK_REQUEST_CODE)
                    return true
                }
            })

            // Purge media files cache
            val pref_clear_media_files_cache: Preference? = findPreference("pref_clear_media_files_cache")
            pref_clear_media_files_cache?.setOnPreferenceClickListener(object :
                Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setTitle(activity?.getString(R.string.alert_dialog_purge_media_cache_title))
                        .setMessage(activity?.getString(R.string.alert_dialog_purge_media_cache_content))
                        .setPositiveButton(android.R.string.ok,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    // Check if path exists
                                    val media_files_path: String? =
                                        MainApplication.get_media_files_path()
                                    if (media_files_path == null)
                                        return
                                    val media_files_path_object = File(media_files_path)
                                    if (!media_files_path_object.isDirectory)
                                        return

                                    val file_list = media_files_path_object.listFiles()
                                    if (file_list != null) {
                                        for (f in file_list) {
                                            if (f.isFile) f.delete()
                                        }
                                        logger(MainApplication.application_context()
                                            .getString(R.string.purge_media_cache_content),
                                            Logger.DEBUG_LEVEL.INFO)
                                    }
                                }
                            })
                        .setNegativeButton(android.R.string.cancel,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    dialog?.cancel()
                                }
                            })
                        .create()
                        .show()
                    return true
                }
            })
        }
    }
}