package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.QRDetectorEngine
import fr.angers.univ.qrludo.engines.coreatoms.*
import fr.angers.univ.qrludo.engines.coreatoms.actions.*
import fr.angers.univ.qrludo.jsonmodels.JSON_QR
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_Question_Exercice
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
 * recognition or QR detection) to a QR code type "Exercice".
 */
/*
------------------------------
Example of generated program:
------------------------------

{ Bool:Answer_0_given(false), Bool:Answer_1_given(false), Bool:Answer_2_given(false), Bool:Answer_3_given(false), Int:Nb_right_answers(0), Int:QR_section(1), Bool:Play_next_section(true) }

 _u_<Play_section_1 @ Int:QR_section(1), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(2)), PrettyPrint(voici ma question : blabla), Speak(voici ma question : blabla), SpeakBeginnerHelp(Un double balayage rapide vers le haut vous permet de basculer entre le mode exploration et le mode de réponse à la question), Add(Bool:QR_start(true)), Add(Bool:Play_next_section(true))>
 _u_<Check_qr_answer @ String:QR_answer(b603d1d0c1be5b769a3df6a84d97e436), Nb_right_answers, Bool:Answer_0_given(false) --> Remove(QR_answer), Add(Bool:Answer_0_given(true)), PrettyPrint(Tu as réussi), Speak(Tu as réussi), Update Number of right answers()>
 _u_<Check_qr_answer_already_givent @ String:QR_answer(b603d1d0c1be5b769a3df6a84d97e436), Bool:Answer_0_given(true) --> Remove(QR_answer), PrettyPrint(Cette réponse a déjà été validée), Speak(Cette réponse a déjà été validée), Add(Bool:QR_start(true))>
 _u_<Check_qr_answer @ String:QR_answer(0edb014beb43cb2a0444fdf816509f7c), Nb_right_answers, Bool:Answer_1_given(false) --> Remove(QR_answer), Add(Bool:Answer_1_given(true)), PrettyPrint(Tu as réussi), Speak(Tu as réussi), Update Number of right answers()>
 _u_<Check_qr_answer_already_givent @ String:QR_answer(0edb014beb43cb2a0444fdf816509f7c), Bool:Answer_1_given(true) --> Remove(QR_answer), PrettyPrint(Cette réponse a déjà été validée), Speak(Cette réponse a déjà été validée), Add(Bool:QR_start(true))>
 _u_<Check_qr_answer @ String:QR_answer(5fe39750db2d845fe2c439169e00137f), Nb_right_answers, Bool:Answer_2_given(false) --> Remove(QR_answer), Add(Bool:Answer_2_given(true)), PrettyPrint(Tu as réussi), Speak(Tu as réussi), Update Number of right answers()>
 _u_<Check_qr_answer_already_givent @ String:QR_answer(5fe39750db2d845fe2c439169e00137f), Bool:Answer_2_given(true) --> Remove(QR_answer), PrettyPrint(Cette réponse a déjà été validée), Speak(Cette réponse a déjà été validée), Add(Bool:QR_start(true))>
 _u_<Check_qr_answer @ String:QR_answer(a5c5c0a2ae660914959057abc47e6bf8), Nb_right_answers, Bool:Answer_3_given(false) --> Remove(QR_answer), Add(Bool:Answer_3_given(true)), PrettyPrint(Tu as réussi), Speak(Tu as réussi), Update Number of right answers()>
 _u_<Check_qr_answer_already_givent @ String:QR_answer(a5c5c0a2ae660914959057abc47e6bf8), Bool:Answer_3_given(true) --> Remove(QR_answer), PrettyPrint(Cette réponse a déjà été validée), Speak(Cette réponse a déjà été validée), Add(Bool:QR_start(true))>
 _u_<Say_wrong_answer @ QR_answer --> Remove(QR_answer), PrettyPrint(Tu as râté), Speak(Tu as râté), Add(Bool:QR_start(true))>
 _u_<Say_exercice_closure @ Int:Nb_right_answers(2) --> Remove(Nb_right_answers), PrettyPrint(Vous avez trouvé toutes les réponses, bravo !!), Speak(Vous avez trouvé toutes les réponses, bravo !!)>
 _u_<Analyse_QR_answer @ QR_code --> Remove(QR_code), Extract QR ID()>
 _u_<Go_to_exploration_mode @ ask_for_backup_user_rules --> Remove(ask_for_backup_user_rules), PrettyPrint(Vous etes en mode exploration), Speak(Vous etes en mode exploration), SpeakBeginnerHelp(Un double balayage rapide vers le haut vous permet de basculer entre le mode exploration et le mode de réponse à la question), Backup user rules and variables(), Add(Bool:QR_start(true))>
 _u_<Go_to_answer_mode @ ask_for_restore_user_rules --> Remove(ask_for_restore_user_rules), PrettyPrint(Vous etes en mode de réponse à la question), Speak(Vous etes en mode de réponse à la question), SpeakBeginnerHelp(Un double balayage rapide vers le haut vous permet de basculer entre le mode exploration et le mode de réponse à la question), Add(Bool:QR_start(true))>
 _u_<Replay_on_QR_abort @ QR_abort --> Remove(QR_abort), Add(Int:QR_section(1)), Add(Bool:Play_next_section(true))>
 _u_<Play_section_clear @ Int:QR_section(2), Play_next_section --> Remove(Play_next_section), Remove(QR_section)>
 _u_<RePlay_QR @ Int:seek_section(-1000) --> Remove(seek_section), Add(Bool:Answer_0_given(false)), Add(Bool:Answer_1_given(false)), Add(Bool:Answer_2_given(false)), Add(Bool:Answer_3_given(false)), Add(Int:Nb_right_answers(0)), Add(Int:QR_section(1)), Go to first()>
 _u_<Stop_mediaPlayer @ Int:seek_section(0) --> Cancel mediaPlayer()>
 _u_<Play_seek @ seek_section, QR_section --> Remove(seek_section), Update QR_section()>
 */
object QR_Exercise_Question_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QRQuestionExercise", msg, level)
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
        logger(context().getString(R.string.action_question_exercice_found), Logger.DEBUG_LEVEL.INFO)

        val gson: Gson = GsonBuilder().create()
        val qe_object : JSON_QR_Question_Exercice = gson.fromJson(data, JSON_QR_Question_Exercice::class.java)

        if ((qe_object.name == null) || (qe_object.nb_min_reponses == null) || (qe_object.data == null)
            || (qe_object.text_bonne_reponse == null) || (qe_object.text_mauvaise_reponse == null)) {
            logger(context().getString(R.string.action_qr_code_missing_field), Logger.DEBUG_LEVEL.ERROR)
            return
        }
        val name: String = qe_object.name!!
        val good_answer: String = qe_object.text_bonne_reponse!!
        val wrong_answer: String = qe_object.text_mauvaise_reponse!!
        val nb_min_answer: Int = qe_object.nb_min_reponses!!

        // Clear all previous user rules
        CoreEngine.clear_user_rules()
        CoreEngine.clear_user_var_store()

        var num_section : Int = 1
        // Add a rule to speak the text of the question
        EngineRule("Play_section_$num_section").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionAddVar(EngineVarInt("QR_section", num_section + 1))
            )
            if(name.startsWith("http://") || name.startsWith("https://"))
                it.add_action(
                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                    ActionPlayMediaURL(name, true)
                )
            else
                it.add_action(
                    ActionPrettyPrint(name),
                    ActionSpeak(name)
                )
            it.add_action(
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.action_question_exercice_mode_help)),
                ActionAddVar(EngineVarBool("QR_start",true)),
                ActionAddVar(EngineVarBool("Play_next_section", true) ))
            CoreEngine.add_user_rule(it)
        }

        // We add rules for each good answer
        var num_rep = 0
        for (br in qe_object.data!!) {
            EngineRule("Check_qr_answer").let {
                it.add_head_atom(EngineVarString("QR_answer", br), false)
                it.add_head_atom(EngineVarInt("Nb_right_answers", 0), true)
                it.add_head_atom(EngineVarBool("Answer_${num_rep}_given", false), false)
                it.add_action(
                    ActionRemoveVar("QR_answer"),
                    ActionAddVar(EngineVarBool("Answer_${num_rep}_given", true))
                )
                if(good_answer.startsWith("http://") || good_answer.startsWith("https://"))
                    it.add_action(
                        ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                        ActionPlayMediaURL(good_answer, true)
                    )
                else
                    it.add_action(
                        ActionPrettyPrint(good_answer),
                        ActionSpeak(good_answer)
                    )
                it.add_action(
                    ActionLambda(
                        "Update Number of right answers"
                    ) { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        var nb_good_answers = 0
                        for (v in head_var_list)
                            if (v._name == "Nb_right_answers") nb_good_answers =
                                (v as EngineVarInt)._value
                        CoreEngine.insert(EngineVarInt("Nb_right_answers", nb_good_answers + 1))
                        if ((nb_good_answers + 1) < nb_min_answer)
                            CoreEngine.insert(EngineVarBool("QR_start", true), call_back_on_finish)
                        else
                            call_back_on_finish()
                    }
                )
                CoreEngine.add_user_rule(it)
            }
            EngineRule("Check_qr_answer_already_given").let {
                it.add_head_atom(EngineVarString("QR_answer", br), false)
                it.add_head_atom(EngineVarBool("Answer_${num_rep}_given", true), false)
                it.add_action(
                    ActionRemoveVar("QR_answer"),
                    ActionPrettyPrint(context().getString(R.string.action_question_exercice_answer_already_given)),
                    ActionSpeak(context().getString(R.string.action_question_exercice_answer_already_given)),
                    ActionAddVar(EngineVarBool("QR_start",true)))
                CoreEngine.add_user_rule(it)
            }
            CoreEngine.insert(EngineVarBool("Answer_${num_rep}_given", false))
            ++num_rep
        }

        // Add fallthrough rule for wrong answer
        EngineRule("Say_wrong_answer").let {
            it.add_head_atom(EngineVarString("QR_answer", ""), true)
            it.add_action(
                ActionRemoveVar("QR_answer")
            )
            if (wrong_answer.startsWith("http://") || wrong_answer.startsWith("https://"))
                it.add_action(
                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                    ActionPlayMediaURL(wrong_answer, true)
                )
            else
                it.add_action(
                    ActionPrettyPrint(wrong_answer),
                    ActionSpeak(wrong_answer)
                )
            it.add_action(
                ActionAddVar(EngineVarBool("QR_start",true))
            )
            CoreEngine.add_user_rule(it)
        }

        // Add a rule for the final closure (when all right answers have been found)
        EngineRule("Say_exercise_closure").let {
            it.add_head_atom(EngineVarInt("Nb_right_answers", nb_min_answer), false)
            it.add_action(
                ActionRemoveVar("Nb_right_answers"),
                ActionPrettyPrint(context().getString(R.string.action_question_exercice_closure)),
                ActionSpeak(context().getString(R.string.action_question_exercice_closure)))
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to launch analyzing of the answer
        EngineRule("Analyse_QR_answer").let {
            it.add_head_atom(EngineVarString("QR_code", ""), true)
            it.add_action(ActionRemoveVar("QR_code"),
                ActionLambda(
                "Extract QR ID"
                ) { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                    var data_qr_code = (head_var_list.first() as EngineVarString)._value
                    // Check is the data_qr_code are base64 encoded
                    if (data_qr_code.matches(Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$"))) {
                        // Start decompress
                        try {
                            data_qr_code = decompress(data_qr_code)
                            logger(
                                context().getString(R.string.action_qranalyse_decompress) + " : $data_qr_code",
                                Logger.DEBUG_LEVEL.VERBOSE
                            )
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    if (data_qr_code.startsWith("{")) {
                        val gson2: Gson = GsonBuilder().create()
                        val converted_json_object: JSON_QR =
                            gson2.fromJson(data_qr_code, JSON_QR::class.java)

                        CoreEngine.insert(
                            EngineVarString(
                                "QR_answer",
                                converted_json_object.id.toString()
                            ), call_back_on_finish
                        )
                    } else
                        call_back_on_finish()
                })
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to go to exploration mode
        EngineRule("Go_to_exploration_mode").let {
            it.add_head_atom(EngineVarBool("ask_for_backup_user_rules", true), true)
            it.add_action(
                ActionRemoveVar("ask_for_backup_user_rules"),
                ActionPrettyPrint(context().getString(R.string.action_question_exercice_go_exploration_mode)),
                ActionSpeak(context().getString(R.string.action_question_exercice_go_exploration_mode)),
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.action_question_exercice_mode_help)),
                ActionLambda(
                    "Backup user rules and variables"
                ) { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                    CoreEngine.backup_user_rules()
                    CoreEngine.clear_user_rules()
                    CoreEngine.clear_user_var_store()
                    call_back_on_finish()
                },
                ActionAddVar(EngineVarBool("QR_start",true))
            )
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to go to answer mode
        EngineRule("Go_to_answer_mode").let {
            it.add_head_atom(EngineVarBool("ask_for_restore_user_rules", true), true)
            it.add_action(
                ActionRemoveVar("ask_for_restore_user_rules"),
                ActionPrettyPrint(context().getString(R.string.action_question_exercice_go_answer_mode)),
                ActionSpeak(context().getString(R.string.action_question_exercice_go_answer_mode)),
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.action_question_exercice_mode_help)),
                ActionAddVar(EngineVarBool("QR_start",true))
            )
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to replay question if we abort QR detection
        EngineRule("Replay_on_QR_abort").let {
            it.add_head_atom(EngineVarString("QR_abort", ""), true)
            it.add_action(
                ActionRemoveVar("QR_abort"),
                ActionAddVar(EngineVarInt("QR_section", 1)),
                ActionAddVar(EngineVarBool("Play_next_section", true))
            )
            CoreEngine.add_user_rule(it)
        }

        ++num_section
        // Add a rule to clear QR code variables
        // and start QR detection
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
            it.add_action(ActionRemoveVar("seek_section"))
            for (i in 0 until num_rep)
                it.add_action(ActionAddVar(EngineVarBool("Answer_${i}_given", false)))
            it.add_action(
                ActionAddVar(EngineVarInt("Nb_right_answers", 0)),
                ActionAddVar(EngineVarInt("QR_section", 1)),
                ActionLambda(
                    "Go to first"
                ) { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                    // Action is handled only when QR detector is scanning
                    if (QRDetectorEngine.is_scanning()) {
                        QRDetectorEngine.cancel()
                        call_back_on_finish()
                    } else {
                        CoreEngine.insert(
                            EngineVarBool("Play_next_section", true),
                            call_back_on_finish
                        )
                    }
                })
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to stop the media player if running
        EngineRule("Stop_mediaPlayer").let {
            it.add_head_atom(EngineVarInt("seek_section", 0), false )
            it.add_action(
                ActionLambda(
                    "Cancel mediaPlayer"
                ) { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                    // Action is handled only when MediaPlayer is running
                    if (MediaPlayerEngine.is_playing())
                        MediaPlayerEngine.stop()
                    call_back_on_finish()
                })
            CoreEngine.add_user_rule(it)
        }

        // Seek to a section left swipe and double left swipe have same effect
        EngineRule("Play_seek").let {
            it.add_head_atom(EngineVarInt("seek_section", 0), true)
            it.add_head_atom(EngineVarInt("QR_section", 0), true)
            it.add_action(ActionRemoveVar("seek_section"),
                ActionLambda(
                    "Update QR_section"
                ) { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                    var seek_section_value = 0
                    for (v in head_var_list) {
                        if (v._name == "seek_section") seek_section_value =
                            (v as EngineVarInt)._value
                    }
                    // We skip to another section
                    // We manage only seek to previous and seek to current and
                    // action is handled only when QR detector is scanning
                    if ((QRDetectorEngine.is_scanning())
                        && (seek_section_value == -1) || (seek_section_value == -2)
                    )
                        CoreEngine.insert(EngineVarInt("QR_section", 1)) {
                            QRDetectorEngine.cancel()
                            call_back_on_finish()
                        }
                    else
                        call_back_on_finish()
                })
            CoreEngine.add_user_rule(it)
        }

        logger(context().getString(R.string.action_new_rules_added) + " : " + CoreEngine.rules_to_string(),
            Logger.DEBUG_LEVEL.INFO)

        // Add initial variables
        CoreEngine.insert( EngineVarInt("Nb_right_answers", 0) )
        CoreEngine.insert( EngineVarInt("QR_section", 1) )
        CoreEngine.insert( EngineVarBool("Play_next_section", true) )
    }
}
