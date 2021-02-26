package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.*
import fr.angers.univ.qrludo.engines.coreatoms.*
import fr.angers.univ.qrludo.engines.coreatoms.actions.*
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_Unique_data
import fr.angers.univ.qrludo.utils.FileDownloader
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Load a user program (set of rules) in the CoreEngine. The user program
 * changes the behaviour of QRLudo to react (playing sound, launch speech
 * recognition or QR detection) to a QR code type "Unique" or "Atomique".
 */
/*
------------------------------
Example of generated program:
------------------------------

{ Int:QR_section(1), Bool:Play_next_section(true) }

 _u_<Play_section_1 @ Int:QR_section(1), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(2)), PrettyPrint(Qr code divisé en 3 parties), Speak(Qr code divisé en 3 parties), Add(Bool:Play_next_section(true))>
 _u_<Play_section_2 @ Int:QR_section(2), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(3)), PrettyPrint(Démarrage de Presentation Table.mp3), Play(https://drive.google.com/uc?id=1NEJyAA54vJ7CsZt2XDG0a22yZoggMjPo&authuser=0&export=download), Add(Bool:Play_next_section(true))>
 _u_<Play_section_3 @ Int:QR_section(3), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(4)), PrettyPrint(Démarrage de Ex Carbone.mp3), Play(https://drive.google.com/uc?id=1-5hYDR6Xws_NYZAxpkm8wmEyFUeSwA7c&authuser=0&export=download), Add(Bool:Play_next_section(true))>
 _u_<Play_section_4 @ Int:QR_section(4), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(5)), PrettyPrint(Démarrage de Ex Magnesium.mp3), Play(https://drive.google.com/uc?id=1oJMdpYk9pYyQM2GER2vcki0LCxko1Ayw&authuser=0&export=download), Add(Bool:Play_next_section(true))>
 _u_<Play_section_clear @ Int:QR_section(5), Play_next_section --> Remove(Play_next_section), Remove(QR_section)>
 _u_<RePlay_QR @ Int:seek_section(-1000) --> Remove(seek_section), Add(Int:QR_section(1)), Go to first()>
 _u_<GoToEnd_QR @ Int:seek_section(1000) --> Remove(seek_section), Add(Int:QR_section(5)), Go to first()>
 _u_<Play_seek @ seek_section, QR_section --> Remove(seek_section), Update QR_section()>
 */
object QR_Unique_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QRUnique", msg, level)
    }

    fun load_from_json(data : String) {
        val gson: Gson = GsonBuilder().create()

        val data_json_object = gson.fromJson(data, JsonObject::class.java)
        val data_json_array = data_json_object.getAsJsonArray("data")
        if (data_json_array != null) {
            // Clear all previous user rules
            CoreEngine.clear_user_rules()
            CoreEngine.clear_user_var_store()

            val files_to_download : MutableList< Pair<String, String> > = mutableListOf()
            var num_section : Int = 0
            // For each section of the data array of the QR code unique
            // A section can be either a "text" either a "music" file
            for (json_section in data_json_array) {
                ++num_section
                try {
                    // The underlying section is a "text"
                    val str: String = gson.fromJson(json_section, String::class.java)
                    // Add a rule to speak a text
                    EngineRule("Play_section_$num_section").let {
                        it.add_head_atom(EngineVarInt("QR_section", num_section), false)
                        it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                        it.add_action(
                            ActionRemoveVar("Play_next_section"),
                            ActionAddVar(EngineVarInt("QR_section", num_section + 1)),
                            ActionPrettyPrint(str),
                            ActionSpeak(str),
                            ActionAddVar(EngineVarBool("Play_next_section", true) ))
                        CoreEngine.add_user_rule(it)
                    }
                } catch (e: Exception) {
                    val json_section_object: JSON_QR_Unique_data = gson.fromJson(json_section, JSON_QR_Unique_data::class.java)
                    if ((json_section_object.url != null) && (json_section_object.type == "music"))
                    {
                        // The underlying section is a "music"
                        // Add the item to the files to download
                        if (json_section_object.name.isNullOrEmpty())
                            files_to_download.add( Pair(json_section_object.url!!, MainApplication.Media_Files_Path + FileDownloader().encode_url(json_section_object.url!!)) )
                        else
                            files_to_download.add( Pair(json_section_object.url!!, MainApplication.Media_Files_Path + json_section_object.name!!) )
                        // Add a rule to play a music
                        EngineRule("Play_section_$num_section").let {
                            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
                            it.add_head_atom(EngineVarBool("Play_next_section", true), true)

                            it.add_action(
                                ActionRemoveVar("Play_next_section"),
                                ActionAddVar(EngineVarInt("QR_section", num_section + 1)))
                            if (json_section_object.name != null)
                                it.add_action(
                                    ActionPrettyPrint(context().getString(R.string.media_playing_start) + json_section_object.name!!),
                                    ActionPlayMediaURL(json_section_object.url!!, true, json_section_object.name!!)
                                )
                            else
                                it.add_action(
                                    ActionPrettyPrint(context().getString(R.string.media_playing_start) + json_section_object.url!!),
                                    ActionPlayMediaURL(json_section_object.url!!, true))
                            it.add_action(ActionAddVar(EngineVarBool("Play_next_section", true)))
                            CoreEngine.add_user_rule(it)
                        }

                    } else {
                        // We don't know this section type
                        logger(context().getString(R.string.action_qranalyse_unique_unkown_section_type) + " : " + json_section_object.type,
                            Logger.DEBUG_LEVEL.ERROR)
                    }
                }
            }

            ++num_section
            // Add a rule to clear QR code variables
            EngineRule("Play_section_clear").let {
                it.add_head_atom(EngineVarInt("QR_section", num_section), false )
                it.add_head_atom(EngineVarBool("Play_next_section", true), true )
                it.add_action(
                    ActionRemoveVar("Play_next_section"),
                    ActionRemoveVar("QR_section"))
                CoreEngine.add_user_rule(it)
            }

            // Add some rules to manage seek actions
            // Add a rule to replay QR code if it is already done
            EngineRule("RePlay_QR").let {
                it.add_head_atom(EngineVarInt("seek_section", -1000), false )
                it.add_action(
                    ActionRemoveVar("seek_section"),
                    ActionAddVar(EngineVarInt("QR_section", 1)),
                    ActionLambda(
                        "Go to first",
                        { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                            // Action is handled only when MediaPlayer is playing
                            if (MediaPlayerEngine.is_playing()) {
                                MediaPlayerEngine.stop()
                                call_back_on_finish()
                            } else {
                                CoreEngine.insert(EngineVarBool("Play_next_section", true), call_back_on_finish)
                            }
                        }
                    ))
                CoreEngine.add_user_rule(it)
            }

            // Add a rule to go to end of QR
            EngineRule("GoToEnd_QR").let {
                it.add_head_atom(EngineVarInt("seek_section", 1000), false )
                it.add_action(
                    ActionRemoveVar("seek_section"),
                    ActionAddVar(EngineVarInt("QR_section", num_section)),
                    ActionLambda(
                        "Go to first",
                        { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                            // Action is handled only when MediaPlayer is playing
                            if (MediaPlayerEngine.is_playing()) {
                                MediaPlayerEngine.stop()
                                call_back_on_finish()
                            } else {
                                CoreEngine.insert(EngineVarBool("Play_next_section", true), call_back_on_finish)
                            }
                        }
                    ))
                CoreEngine.add_user_rule(it)
            }

            // Seek to a section
            EngineRule("Play_seek").let {
                it.add_head_atom(EngineVarInt("seek_section", 0), true)
                it.add_head_atom(EngineVarInt("QR_section", 0), true)
                it.add_action(ActionRemoveVar("seek_section"),
                    ActionLambda(
                        "Update QR_section",
                        { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                            var seek_section_value = 0
                            var qr_section_value = 0
                            for (v in head_var_list) {
                                if (v._name == "seek_section") seek_section_value =
                                    (v as EngineVarInt)._value
                                if (v._name == "QR_section") qr_section_value =
                                    (v as EngineVarInt)._value
                            }
                            // We skip to another section
                            // action is handled only when MediaPlayer is playing
                            if (MediaPlayerEngine.is_playing()) {
                                val new_num_section =
                                    if ((qr_section_value + seek_section_value) < 1)
                                        1
                                    else if ((qr_section_value + seek_section_value) > num_section)
                                        num_section
                                    else
                                        qr_section_value + seek_section_value
                                CoreEngine.insert(EngineVarInt("QR_section", new_num_section), { ->
                                    MediaPlayerEngine.stop()
                                    call_back_on_finish()
                                })
                            }
                            else
                                call_back_on_finish()
                        }
                    ))
                CoreEngine.add_user_rule(it)
            }

            logger(context().getString(R.string.action_new_rules_added) + " : " + CoreEngine.rules_to_string(), Logger.DEBUG_LEVEL.INFO)
            // Add initial variables
            CoreEngine.insert( EngineVarInt("QR_section", 1) )

            TTSEngine.text_to_file(context().getString(R.string.beginner_help_file_download_start),
                { file_name: String ->
                    // We do the work in the callback to be sure that the TTSEngine is idle
                    if (files_to_download.isNotEmpty() && !MainApplication.Expert_Mode) {
                        MediaPlayerEngine.stop()
                        MediaPlayerEngine.play(
                            file_name, { _: String -> })
                    }
                    // Download all files and start playing when everything is downloaded
                    FileDownloader().download_files(files_to_download, { ->
                        CoreEngine.insert(EngineVarBool("Play_next_section", true))
                    })
                })

        } else {
            logger(context().getString(R.string.action_qranalyse_unique_no_data),
                Logger.DEBUG_LEVEL.ERROR)
        }
    }
}