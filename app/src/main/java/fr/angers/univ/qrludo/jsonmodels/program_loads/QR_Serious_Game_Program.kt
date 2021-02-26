package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.QRDetectorEngine
import fr.angers.univ.qrludo.engines.SpeechRecognitionEngine
import fr.angers.univ.qrludo.engines.coreatoms.*
import fr.angers.univ.qrludo.engines.coreatoms.actions.*
import fr.angers.univ.qrludo.jsonmodels.JSON_QR
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_Serious_Game
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * Load a user program (set of rules) in the CoreEngine. The user program
 * changes the behaviour of QRLudo to react (playing sound, launch speech
 * recognition or QR detection) to a QR code type "Serious_game".
 */
/*
------------------------------
Example of generated program:
------------------------------

{ Bool:SG_puzzle_1(true), Bool:SG_puzzle_2(true), Int:SG_nb_unsolved_puzzle(2), Int:QR_section(1), Bool:Play_next_section(true) }

 _u_<Play_section_1 @ Int:QR_section(1), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(2)), PrettyPrint(c'est l'introduction du jeu), Speak(c'est l'introduction du jeu), Add(Bool:Play_next_section(true))>
 _u_<Play_section_2 @ Int:QR_section(2), Play_next_section --> Remove(Play_next_section), Remove(SG_puzzle_choice), Add(Int:QR_section(3)), Speak(Choisissez votre destination parmi la liste suivante), Add(Int:SG_start_listing_available_puzzles(1)), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Bool:SR_start(true)), Add(Bool:Play_next_section(true))>
 _u_<Say_puzzle_name_if_available @ Int:SG_start_listing_available_puzzles(1), SG_puzzle_1 --> Speak(première énigme qr), Add(Int:SG_start_listing_available_puzzles(2))>
 _u_<Dont_say_puzzle_name_if_not_available @ Int:SG_start_listing_available_puzzles(1) --> Add(Int:SG_start_listing_available_puzzles(2))>
 _u_<Check_puzzle_choice @ SG_puzzle_1, Int:QR_section(3), Regex:SR_text[(première énigme qr)] --> Remove(SR_text), Add(Int:SG_puzzle_choice(1))>
 _u_<Say_puzzle_name_if_available @ Int:SG_start_listing_available_puzzles(2), SG_puzzle_2 --> Speak(ma deuxième énigme), Add(Int:SG_start_listing_available_puzzles(3))>
 _u_<Dont_say_puzzle_name_if_not_available @ Int:SG_start_listing_available_puzzles(2) --> Add(Int:SG_start_listing_available_puzzles(3))>
 _u_<Check_puzzle_choice @ SG_puzzle_2, Int:QR_section(3), Regex:SR_text[(ma deuxième énigme)] --> Remove(SR_text), Add(Int:SG_puzzle_choice(2))>
 _u_<End_of_listing_puzzle @ Int:SG_start_listing_available_puzzles(3) --> Remove(SG_start_listing_available_puzzles)>
 _u_<Say_unkown_puzzle @ Int:QR_section(3), SR_text --> Remove(SR_text), Speak(Je n'ai pas compris votre destination, pouvez-vous répéter ?), Add(Bool:SR_start(true))>
 _u_<Say_unkown_puzzle_on_error @ Int:QR_section(3), SR_error --> Remove(SR_error), Speak(Je n'ai pas compris votre destination, pouvez-vous répéter ?), Add(Bool:SR_start(true))>
 _u_<Play_section_3 @ Int:SG_puzzle_choice(1), Int:QR_section(3), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(4)), Speak(Quel outil prendre ), Add(Bool:QR_start(true)), Add(Bool:Play_next_section(true))>
 _u_<Check_puzzle_1_answer @ Int:SG_puzzle_choice(1), Int:QR_section(4), String:QR_answer(marteau), SG_nb_unsolved_puzzle --> Remove(QR_answer), Remove(SG_puzzle_choice), Remove(SG_puzzle_1), Speak(Vous avez résolu cette énigme), Update Number of unsolved puzzles()>
 _u_<Check_puzzle_1_wrong_answer @ Int:SG_puzzle_choice(1), Int:QR_section(4), String:QR_answer(tournevis) --> Remove(QR_answer), Speak(Mauvaise réponse, essayez encore), Add(Bool:QR_start(true))>
 _u_<Check_puzzle_wrong_qr_code_type @ Int:QR_section(4), QR_answer --> Remove(QR_answer), Speak(Mauvaise réponse, essayez encore), Add(Bool:QR_start(true))>
 _u_<Analyse_QR_answer @ QR_code --> Remove(QR_code), Extract QR Name()>
 _u_<Play_section_3 @ Int:SG_puzzle_choice(2), Int:QR_section(3), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(4)), Speak(Combien font 5 fois 5), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Bool:SR_start(true)), Add(Bool:Play_next_section(true))>
 _u_<Check_puzzle_2_answer @ Int:SG_puzzle_choice(2), Int:QR_section(4), Regex:SR_text[25], SG_nb_unsolved_puzzle --> Remove(SR_text), Remove(SG_puzzle_choice), Remove(SG_puzzle_2), Speak(Vous avez résolu cette énigme), Update Number of unsolved puzzles()>
 _u_<Say_wrong_puzzle_answer @ Int:SG_puzzle_choice(2), Int:QR_section(4), SR_text --> Remove(SR_text), Speak(Mauvaise réponse, essayez encore), Add(Bool:SR_start(true))>
 _u_<Say_puzzle_answer_not_undestand @ Int:SG_puzzle_choice(2), Int:QR_section(4), SR_error --> Remove(SR_error), Speak(Je n'ai pas compris votre réponse), Add(Bool:SR_start(true))>
 _u_<Play_section_4 @ Int:SG_nb_unsolved_puzzle(0), Int:QR_section(4), Play_next_section --> Remove(Play_next_section), Remove(SG_nb_unsolved_puzzle), Add(Int:QR_section(5)), PrettyPrint(C'est la fin du jeu), Speak(C'est la fin du jeu), Add(Bool:Play_next_section(true))>
 _u_<Play_section_4 @ Int:QR_section(4), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(2)), Add(Bool:Play_next_section(true))>
 _u_<Play_section_clear @ Int:QR_section(5), Play_next_section --> Remove(Play_next_section), Remove(QR_section)>
 _u_<RePlay_QR @ Int:seek_section(-1000) --> Remove(Play_next_section), Remove(seek_section), Remove(SG_start_listing_available_puzzles), Remove(SG_puzzle_choice), Add(Bool:SG_puzzle_1(true)), Add(Bool:SG_puzzle_2(true)), Add(Int:SG_nb_unsolved_puzzle(2)), Add(Int:QR_section(1)), Go to first()>
 _u_<Stop_mediaPlayer @ Int:seek_section(0) --> Cancel mediaPlayer()>
 _u_<Play_seek @ seek_section, QR_section --> Remove(seek_section), Update QR_section()>
 */
object QR_Serious_Game_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QRSeriousGame", msg, level)
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

    fun load_from_json(data: String) {
        logger(context().getString(R.string.action_serious_game_found), Logger.DEBUG_LEVEL.INFO)

        val gson: Gson = GsonBuilder().create()
        val sg_object : JSON_QR_Serious_Game = gson.fromJson(data, JSON_QR_Serious_Game::class.java)

        val puzzles_json_object = gson.fromJson(data, JsonObject::class.java)
        val puzzles_json_array = puzzles_json_object.getAsJsonArray("enigmes")
        val puzzles_qr_json_array = puzzles_json_object.getAsJsonArray("questionsQrCode")
        val puzzles_sr_json_array = puzzles_json_object.getAsJsonArray("questionsRecoVocale")
        val list_of_puzzle_name_id : MutableList<String> = mutableListOf()

        if ((sg_object.name == null) || (puzzles_json_object == null)
            || (sg_object.introduction == null) || (sg_object.fin == null)) {
            logger(context().getString(R.string.action_qr_code_missing_field), Logger.DEBUG_LEVEL.ERROR)
            return
        }

        // Clear all previous user rules
        CoreEngine.clear_user_rules()
        CoreEngine.clear_user_var_store()

        // ------------------------------------------------
        // ---------- INTRODUCTION
        // ------------------------------------------------
        var num_section : Int = 1
        // Add a rule to say the text of the introduction
        EngineRule("Play_section_$num_section").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionAddVar(EngineVarInt("QR_section", num_section + 1)))
            if (sg_object.introduction!!.startsWith("http://") || sg_object.introduction!!.startsWith("https://"))
                it.add_action(
                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                    ActionPlayMediaURL(sg_object.introduction!!, true)
                )
            else
                it.add_action(
                    ActionPrettyPrint(sg_object.introduction!!),
                    ActionSpeak(sg_object.introduction!!))
            it.add_action(
                ActionAddVar(EngineVarBool("Play_next_section", true)))
            CoreEngine.add_user_rule(it)
        }

        // ------------------------------------------------
        // ---------- LIST OF PUZZLES
        // ------------------------------------------------
        ++num_section
        // Add a rule to say the list of puzzle
        EngineRule("Play_section_$num_section").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionRemoveVar("SG_puzzle_choice"),
                ActionAddVar(EngineVarInt("QR_section", num_section + 1)),
                ActionSpeak(context().getString(R.string.action_puzzle_possible_puzzle)),
                ActionAddVar(EngineVarInt("SG_start_listing_available_puzzles",1)),
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.action_vocal_beginner_help)),
                ActionAddVar(EngineVarBool("SR_start",true)),
                ActionAddVar(EngineVarBool("Play_next_section", true)))
            CoreEngine.add_user_rule(it)
        }

        // We add rules for chosing which puzzle to solve
        for (puz in puzzles_json_array) {
            val puz_array = puz.asJsonArray
            val t_puz_id: Int = gson.fromJson(puz_array[0], String::class.java).toInt()
            val t_puz_name: String = gson.fromJson(puz_array[1], String::class.java)

            // Create a var (which will be added at the end) to say that puzzle is still there (not already solved)
            list_of_puzzle_name_id.add("SG_puzzle_$t_puz_id")
            // Add two rules to recusively list (and speak) the available puzzles
            EngineRule("Say_puzzle_name_if_available").let {
                it.add_head_atom(EngineVarInt("SG_start_listing_available_puzzles", t_puz_id), false)
                it.add_head_atom(EngineVarBool("SG_puzzle_$t_puz_id", true), true)
                it.add_action(
                    ActionSpeak(t_puz_name),
                    ActionAddVar(EngineVarInt("SG_start_listing_available_puzzles", t_puz_id+1)))
                CoreEngine.add_user_rule(it)
            }
            EngineRule("Dont_say_puzzle_name_if_not_available").let {
                it.add_head_atom(EngineVarInt("SG_start_listing_available_puzzles", t_puz_id), false)
                it.add_action(ActionAddVar(EngineVarInt("SG_start_listing_available_puzzles", t_puz_id+1)))
                CoreEngine.add_user_rule(it)
            }

            // Add a rule to post the puzzle choice
            EngineRule("Check_puzzle_choice").let {
                it.add_head_atom(EngineVarBool("SG_puzzle_$t_puz_id", true), true)
                it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
                it.add_head_atom(EngineVarRegex("SR_text", "($t_puz_name)"), false)
                it.add_action(
                    ActionRemoveVar("SR_text"),
                    ActionAddVar(EngineVarInt("SG_puzzle_choice", t_puz_id)))
                CoreEngine.add_user_rule(it)
            }
        }
        EngineRule("End_of_listing_puzzle").let {
            it.add_head_atom(EngineVarInt("SG_start_listing_available_puzzles", puzzles_json_array.size()+1), false)
            it.add_action(ActionRemoveVar("SG_start_listing_available_puzzles"))
            CoreEngine.add_user_rule(it)
        }

        // Add failthrough rule for unknown puzzle choice
        EngineRule("Say_unkown_puzzle").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
            it.add_head_atom(EngineVarString("SR_text", ""), true)
            it.add_action(
                ActionRemoveVar("SR_text"),
                ActionSpeak(context().getString(R.string.action_puzzle_unknown)),
                ActionAddVar(EngineVarBool("SR_start",true)))
            CoreEngine.add_user_rule(it)
        }
        EngineRule("Say_unkown_puzzle_on_error").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
            it.add_head_atom(EngineVarString("SR_error", ""), true)
            it.add_action(
                ActionRemoveVar("SR_error"),
                ActionSpeak(context().getString(R.string.action_puzzle_unknown)),
                ActionAddVar(EngineVarBool("SR_start",true)))
            CoreEngine.add_user_rule(it)
        }

        // ------------------------------------------------
        // ---------- LIST OF PUZZLES
        // ------------------------------------------------
        ++num_section
        // We add rules for each qr code puzzle
        for (puz in puzzles_qr_json_array) {
            val puz_array = puz.asJsonArray
            val t_puz_id: Int = gson.fromJson(puz_array[0], String::class.java).toInt()
            val t_puz_name: String = gson.fromJson(puz_array[1], String::class.java)
            val t_puz_answers = puz_array[2].asJsonArray

            // Add a rule to say the text of the puzzle
            EngineRule("Play_section_$num_section").let {
                it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                it.add_head_atom(EngineVarInt("QR_section", num_section), false)
                it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                it.add_action(
                    ActionRemoveVar("Play_next_section"),
                    ActionAddVar(EngineVarInt("QR_section", num_section + 1)))
                if (t_puz_name.startsWith("http://") || t_puz_name.startsWith("https://"))
                    it.add_action(
                        ActionPlayMediaURL(t_puz_name, true)
                    )
                else
                    it.add_action(
                        ActionSpeak(t_puz_name))
                it.add_action(
                    ActionAddVar(EngineVarBool("QR_start",true)),
                    ActionAddVar(EngineVarBool("Play_next_section", true)))
                CoreEngine.add_user_rule(it)
            }

            // For each answer
            for (a in t_puz_answers) {
                val puz_answer_array = a.asJsonArray
                val t_puz_anwser_name: String = gson.fromJson(puz_answer_array[0], String::class.java)
                val t_puz_answer_good: Boolean = gson.fromJson(puz_answer_array[1], String::class.java).toBoolean()

                if (t_puz_answer_good)
                    // Add a rule when the puzzle is solved
                    EngineRule("Check_puzzle_${t_puz_id}_answer").let {
                        it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                        it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
                        it.add_head_atom(EngineVarString("QR_answer", t_puz_anwser_name), false)
                        it.add_head_atom(EngineVarInt("SG_nb_unsolved_puzzle", 0), true)
                        it.add_action(
                            ActionRemoveVar("QR_answer"),
                            ActionRemoveVar("SG_puzzle_choice"),
                            ActionRemoveVar("SG_puzzle_$t_puz_id"),
                            ActionSpeak(context().getString(R.string.action_puzzle_one_solved)),
                            ActionLambda(
                                "Update Number of unsolved puzzles",
                                { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                                    var nb_unsolved_puzzles = 0
                                    for (v in head_var_list)
                                        if (v._name == "SG_nb_unsolved_puzzle") nb_unsolved_puzzles = (v as EngineVarInt)._value
                                    CoreEngine.insert(EngineVarInt("SG_nb_unsolved_puzzle", nb_unsolved_puzzles - 1), call_back_on_finish)
                                }
                            ))
                        CoreEngine.add_user_rule(it)
                    }
                else
                    // Add a rule when a wrong answer has been given
                    EngineRule("Check_puzzle_${t_puz_id}_wrong_answer").let {
                        it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                        it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
                        it.add_head_atom(EngineVarString("QR_answer", t_puz_anwser_name), false)
                        it.add_action(
                            ActionRemoveVar("QR_answer"),
                            ActionSpeak(context().getString(R.string.action_puzzle_wrong_answer)),
                            ActionAddVar(EngineVarBool("QR_start",true)))
                        CoreEngine.add_user_rule(it)
                    }
            }
        }

        // Add a failtrough rule when a wrong QR code type is scanned
        EngineRule("Check_puzzle_wrong_qr_code_type").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
            it.add_head_atom(EngineVarString("QR_answer", ""), true)
            it.add_action(
                ActionRemoveVar("QR_answer"),
                ActionSpeak(context().getString(R.string.action_puzzle_wrong_answer)),
                ActionAddVar(EngineVarBool("QR_start",true)))
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to launch analyzing of the answer
        EngineRule("Analyse_QR_answer").let {
            it.add_head_atom(EngineVarString("QR_code", ""), true)
            it.add_action(ActionRemoveVar("QR_code"),
                ActionLambda(
                    "Extract QR Name",
                    { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        var data_qr_code = (head_var_list.first() as EngineVarString)._value
                        // Check is the data_qr_code are base64 encoded
                        if (data_qr_code.matches(Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) ) {
                            // Début de la décompression
                            try {
                                data_qr_code = decompress(data_qr_code)
                                logger(context().getString(R.string.action_qranalyse_decompress) + " : $data_qr_code",
                                    Logger.DEBUG_LEVEL.VERBOSE)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }

                        if (data_qr_code.startsWith("{")) {
                            val gson2: Gson = GsonBuilder().create()
                            val converted_json_object: JSON_QR = gson2.fromJson(data_qr_code, JSON_QR::class.java)
                            if (converted_json_object.name != null) {
                                CoreEngine.insert(EngineVarString("QR_answer", converted_json_object.name!!), call_back_on_finish)
                                return@ActionLambda
                            }
                        }
                        CoreEngine.insert(EngineVarBool("QR_start", true), call_back_on_finish)
                    }
                ))
            CoreEngine.add_user_rule(it)
        }

        // We add rules for each speech recognition puzzle
        for (puz in puzzles_sr_json_array) {
            val puz_array = puz.asJsonArray
            val t_puz_id: Int = gson.fromJson(puz_array[0], String::class.java).toInt()
            val t_puz_name: String = gson.fromJson(puz_array[1], String::class.java)
            val t_puz_answer : String = gson.fromJson(puz_array[2], String::class.java)

            // Add a rule to say the text of the puzzle
            EngineRule("Play_section_$num_section").let {
                it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                it.add_head_atom(EngineVarInt("QR_section", num_section), false)
                it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                it.add_action(
                    ActionRemoveVar("Play_next_section"),
                    ActionAddVar(EngineVarInt("QR_section", num_section + 1)))
                if (t_puz_name.startsWith("http://") || t_puz_name.startsWith("https://"))
                    it.add_action(
                        ActionPlayMediaURL(t_puz_name, true)
                    )
                else
                    it.add_action(
                        ActionSpeak(t_puz_name))
                it.add_action(
                    ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.action_vocal_beginner_help)),
                    ActionAddVar(EngineVarBool("SR_start",true)),
                    ActionAddVar(EngineVarBool("Play_next_section", true)))
                CoreEngine.add_user_rule(it)
            }

            // Add a rule when the puzzle is solved
            EngineRule("Check_puzzle_${t_puz_id}_answer").let {
                it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
                it.add_head_atom(EngineVarRegex("SR_text", t_puz_answer), false)
                it.add_head_atom(EngineVarInt("SG_nb_unsolved_puzzle", 0), true)
                it.add_action(
                    ActionRemoveVar("SR_text"),
                    ActionRemoveVar("SG_puzzle_choice"),
                    ActionRemoveVar("SG_puzzle_$t_puz_id"),
                    ActionSpeak(context().getString(R.string.action_puzzle_one_solved)),
                    ActionLambda(
                        "Update Number of unsolved puzzles",
                        { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                            var nb_unsolved_puzzles = 0
                            for (v in head_var_list)
                                if (v._name == "SG_nb_unsolved_puzzle") nb_unsolved_puzzles = (v as EngineVarInt)._value
                            CoreEngine.insert(EngineVarInt("SG_nb_unsolved_puzzle", nb_unsolved_puzzles - 1), call_back_on_finish)
                        }
                    ))
                CoreEngine.add_user_rule(it)
            }

            // Add a rule for wrong puzzle answer
            EngineRule("Say_wrong_puzzle_answer").let {
                it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
                it.add_head_atom(EngineVarRegex("SR_text", ""), true)
                it.add_action(
                    ActionRemoveVar("SR_text"),
                    ActionSpeak(context().getString(R.string.action_puzzle_wrong_answer)),
                    ActionAddVar(EngineVarBool("SR_start",true)))
                CoreEngine.add_user_rule(it)
            }

            // Add a rule for text not understood
            EngineRule("Say_puzzle_answer_not_undestand").let {
                it.add_head_atom(EngineVarInt("SG_puzzle_choice", t_puz_id), false)
                it.add_head_atom(EngineVarInt("QR_section", num_section+1), false)
                it.add_head_atom(EngineVarRegex("SR_error", ""), true)
                it.add_action(
                    ActionRemoveVar("SR_error"),
                    ActionSpeak(context().getString(R.string.action_puzzle_answer_dont_understand)),
                    ActionAddVar(EngineVarBool("SR_start",true)))
                CoreEngine.add_user_rule(it)
            }
        }

        // ------------------------------------------------
        // ---------- CONCLUSION
        // ------------------------------------------------
        ++num_section
        // Add a rule to say the text of the conclusion
        EngineRule("Play_section_$num_section").let {
            it.add_head_atom(EngineVarInt("SG_nb_unsolved_puzzle", 0), false)
            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionRemoveVar("SG_nb_unsolved_puzzle"),
                ActionAddVar(EngineVarInt("QR_section", num_section + 1)))
            if (sg_object.fin!!.startsWith("http://") || sg_object.fin!!.startsWith("https://"))
                it.add_action(
                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                    ActionPlayMediaURL(sg_object.fin!!, true)
                )
            else
                it.add_action(
                    ActionPrettyPrint(sg_object.fin!!),
                    ActionSpeak(sg_object.fin!!))
            it.add_action(ActionAddVar(EngineVarBool("Play_next_section", true)))
            CoreEngine.add_user_rule(it)
        }

        // If we don't go to the conclusion, then some puzzle still need to be solved
        EngineRule("Play_section_$num_section").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionAddVar(EngineVarInt("QR_section", num_section - 2)),
                ActionAddVar(EngineVarBool("Play_next_section", true) ))
            CoreEngine.add_user_rule(it)
        }

        // ------------------------------------------------
        // ---------- PROGRAM NEEDED RULES
        // ------------------------------------------------
        ++num_section
        // Add a rule to clear QR code variables when we are at the end of the serious game
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
                ActionRemoveVar("Play_next_section"),
                ActionRemoveVar("seek_section"),
                ActionRemoveVar("SG_start_listing_available_puzzles"),
                ActionRemoveVar("SG_puzzle_choice"))
            // Add initial variables
            for (puz_id in list_of_puzzle_name_id)
            // Add a var to say that puzzle is still there (not already solved)
                it.add_action(ActionAddVar(EngineVarBool(puz_id,true)))
            it.add_action(
                ActionAddVar(EngineVarInt("SG_nb_unsolved_puzzle", puzzles_json_array.size())),
                ActionAddVar(EngineVarInt("QR_section", 1)),
                ActionLambda(
                    "Go to first",
                    { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        // Action is handled only when in a Speech Recognition or a QR code scanning
                        if (SpeechRecognitionEngine.is_recording() || QRDetectorEngine.is_scanning()) {
                            SpeechRecognitionEngine.cancel()
                            QRDetectorEngine.cancel()
                            call_back_on_finish()
                        } else {
                            CoreEngine.insert(EngineVarBool("Play_next_section", true), call_back_on_finish)
                        }
                    }
                ))
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to stop the media player if running
        EngineRule("Stop_mediaPlayer").let {
            it.add_head_atom(EngineVarInt("seek_section", 0), false )
            it.add_action(
                ActionLambda(
                    "Cancel mediaPlayer",
                    { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        // Action is handled only when MediaPlayer is running
                        if (MediaPlayerEngine.is_playing())
                            MediaPlayerEngine.stop()
                        call_back_on_finish()
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
                        // We manage only seek to previous and seek to current and
                        // action is handled only when in a Speech Recognition or a QR code scanning
                        if ((SpeechRecognitionEngine.is_recording() || QRDetectorEngine.is_scanning())
                            && (seek_section_value == -1) || (seek_section_value == -2))
                            CoreEngine.insert(EngineVarInt("QR_section", maxOf(1, qr_section_value + seek_section_value)), { ->
                                SpeechRecognitionEngine.cancel()
                                QRDetectorEngine.cancel()
                                call_back_on_finish()
                            })
                        else
                            call_back_on_finish()
                    }
                ))
            CoreEngine.add_user_rule(it)
        }

        logger(context().getString(R.string.action_new_rules_added) + " : " + CoreEngine.rules_to_string(),
            Logger.DEBUG_LEVEL.INFO)

        // Add initial variables
        for (puz_id in list_of_puzzle_name_id)
            // Add a var to say that puzzle is still there (not already solved)
            CoreEngine.insert(EngineVarBool(puz_id,true))

        CoreEngine.insert(EngineVarInt("SG_nb_unsolved_puzzle", puzzles_json_array.size()))
        CoreEngine.insert(EngineVarInt("QR_section", 1))
        CoreEngine.insert(EngineVarBool("Play_next_section", true))
    }
}