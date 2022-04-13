package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
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
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_QCM
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Load a user program (set of rules) in the CoreEngine. The user program
 * changes the behaviour of QRLudo to react (playing sound, launch speech
 * recognition or QR detection) to a QR code type "Vocal QCM".
 */
/*
------------------------------
Example of generated program:
------------------------------

{ Int:QR_section(1), Bool:Play_next_section(true) }

 _u_<Check_right_answer @ Regex:SR_text[(Paris|réponse numéro 1)], Int:QR_question(1) --> Remove(SR_text), PrettyPrint(Bravo), Speak(Bravo), Add(Int:QR_section(2)), Add(Bool:Play_next_section(true))>
 _u_<Check_wrong_answer @ Regex:SR_text[(Tours|réponse numéro 2)], Int:QR_question(1) --> Remove(SR_text), PrettyPrint(Dommage), Speak(Dommage), Add(Bool:SR_start(true))>
 _u_<Check_wrong_answer @ Regex:SR_text[(Angers|réponse numéro 3)], Int:QR_question(1) --> Remove(SR_text), PrettyPrint(Dommage), Speak(Dommage), Add(Bool:SR_start(true))>
 _u_<Check_right_answer @ Regex:SR_text[(Madrid|réponse numéro 1)], Int:QR_question(2) --> Remove(SR_text), PrettyPrint(Bravo), Speak(Bravo), Add(Int:QR_section(3)), Add(Bool:Play_next_section(true))>
 _u_<Check_wrong_answer @ Regex:SR_text[(Barcelone|réponse numéro 2)], Int:QR_question(2) --> Remove(SR_text), PrettyPrint(Dommage), Speak(Dommage), Add(Bool:SR_start(true))>
 _u_<Check_wrong_answer @ Regex:SR_text[(Séville|réponse numéro 3)], Int:QR_question(2) --> Remove(SR_text), PrettyPrint(Dommage), Speak(Dommage), Add(Bool:SR_start(true))>
 _u_<Play_section_1 @ Int:QR_section(1), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Quel est la capitale de la France), Speak(question numéro 1 Quel est la capitale de la France), Speak(Les réponses possibles sont), Speak(réponse numéro 1  Paris), Speak(réponse numéro 2  Tours), Speak(réponse numéro 3  Angers), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Int:QR_question(1)), Add(Bool:SR_start(true))>
 _u_<Check_right_answer @ Regex:SR_text[(Paris|réponse numéro 1)], Int:QR_question(1) --> Remove(SR_text), PrettyPrint(Bravo), Speak(Bravo), Add(Int:QR_section(2)), Add(Bool:Play_next_section(true))>
 _u_<Play_section_2 @ Int:QR_section(2), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Quel est la capitale de l'Espagne), Speak(question numéro 2 Quel est la capitale de l'Espagne), Speak(Les réponses possibles sont), Speak(réponse numéro 1  Madrid), Speak(réponse numéro 2  Barcelone), Speak(réponse numéro 3  Séville), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Int:QR_question(2)), Add(Bool:SR_start(true))>
 _u_<Say_unkown_answer @ SR_text --> Remove(SR_text), Speak(Votre réponse ne fait pas partie de la liste des réponses possibles), Add(Bool:SR_start(true))>
 _u_<Say_unrecognize_answer @ SR_error --> Remove(SR_error), Speak(Je n'ai pas reconnu votre réponse), Add(Bool:SR_start(true))>
 _u_<Replay_on_QR_abort @ SR_abort --> Remove(SR_abort), Add(Int:QR_section(2)), Add(Bool:Play_next_section(true))>
 _u_<Play_section_end @ Int:QR_section(3), Play_next_section --> Remove(Play_next_section), Remove(QR_section), PrettyPrint(Vous avez fini le QCM), Speak(Vous avez fini le QCM), Add(Int:QR_section(4)), Add(Bool:Play_next_section(true))>
 _u_<Play_section_clear @ Int:QR_section(4), Play_next_section --> Remove(Play_next_section), Remove(QR_section)>
 _u_<Stop_mediaPlayer @ Int:seek_section(0) --> Cancel mediaPlayer()>
 _u_<Play_seek @ seek_section, QR_section --> Remove(seek_section), Update QR_section()>

*/
object QR_Vocal_QCM_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QRQCM", msg, level)
    }

    fun load_from_json(data: String) {
        logger(context().getString(R.string.action_qcm_found), Logger.DEBUG_LEVEL.INFO)

        var num_section: Int = 1
        val gson: Gson = GsonBuilder().create()
        val qe_object : JSON_QR_QCM = gson.fromJson(data, JSON_QR_QCM::class.java)
        var answers: LinkedHashMap<String, String> = LinkedHashMap()
        val question_json_object = gson.fromJson(data, JsonObject::class.java)
        val question_json_array = question_json_object.getAsJsonArray("questions")
        val good_answer_object: JsonObject=qe_object.text_bonne_reponse!!
        val wrong_answer_object: JsonObject=qe_object.text_mauvaise_reponse!!
        var good_answer: String
        var wrong_answer: String
        val good_answer_type=gson.fromJson(good_answer_object.getAsJsonPrimitive("type"),String::class.java)
        val wrong_answer_type=gson.fromJson(wrong_answer_object.getAsJsonPrimitive("type"),String::class.java)

        if(good_answer_type.equals("text")){
            good_answer=gson.fromJson(good_answer_object.getAsJsonPrimitive("text"),String::class.java)
        }
        else{
            good_answer=gson.fromJson(good_answer_object.getAsJsonPrimitive("url"),String::class.java)
        }
        if(wrong_answer_type.equals("text")){
            wrong_answer=gson.fromJson(wrong_answer_object.getAsJsonPrimitive("text"),String::class.java)
        }
        else{
            wrong_answer=gson.fromJson(wrong_answer_object.getAsJsonPrimitive("url"),String::class.java)
        }

        // Clear all previous user rules
        CoreEngine.clear_user_rules()
        CoreEngine.clear_user_var_store()

        // We add rules for each question
        for (que in question_json_array) {
            val que_array = que.asJsonObject
            val que_object =
                gson.fromJson(que_array.getAsJsonObject("question"), JsonObject::class.java)
            var question_text_objet: JsonObject =
                gson.fromJson(que_object.getAsJsonObject("textQuestion"), JsonObject::class.java)
            val question_text_type=gson.fromJson(question_text_objet.getAsJsonPrimitive("type"),String::class.java)
            var question_text:String
            val dat_json_object = que_object.getAsJsonArray("reponses")

            if(question_text_type.equals("text"))
                question_text=gson.fromJson(question_text_objet.getAsJsonPrimitive("text"),String::class.java)
            else
                question_text=gson.fromJson(question_text_objet.getAsJsonPrimitive("url"), String::class.java)

            // We add rules for each answer
            for (answer in dat_json_object) {
                val answer_array = answer.asJsonArray
                val t_1: String=gson.fromJson(answer_array[0],String::class.java)
                val t_2: String=gson.fromJson(answer_array[1],String::class.java)
                val t_3: Boolean=gson.fromJson(answer_array[2],Boolean::class.java)
                answers["($t_1)"] = "($t_2)"
                val regex = "($t_2|$t_1)"
                if (t_3)
                //add rule if it's good answer
                    EngineRule("Check_right_answer").let {
                        it.add_head_atom(EngineVarRegex("SR_text", regex, true), false)
                        it.add_head_atom(EngineVarInt("QR_question", num_section), false)
                        it.add_action(
                            ActionRemoveVar("SR_text"),
                        )
                        if (good_answer_type.equals("music"))
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
                            ActionAddVar(EngineVarInt("QR_section", num_section +1)),
                            ActionAddVar(EngineVarBool("Play_next_section", true))
                        )
                        CoreEngine.add_user_rule(it)
                    }
                else
                //add rule if it's wrong answer
                    EngineRule("Check_wrong_answer").let {
                        it.add_head_atom(EngineVarRegex("SR_text", regex, true), false)
                        it.add_head_atom(EngineVarInt("QR_question", num_section), false)
                        it.add_action(
                            ActionRemoveVar("SR_text")
                        )
                        if (wrong_answer_type.equals("music"))
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
                            ActionAddVar(EngineVarBool("SR_start", true))
                        )
                        CoreEngine.add_user_rule(it)
                    }
            }

            //add rule to speak question and start vocal recognition
            EngineRule("Play_section_$num_section").let {
                it.add_head_atom(EngineVarInt("QR_section", num_section), false)
                it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                it.add_action(
                    ActionRemoveVar("Play_next_section"),
                    ActionRemoveVar("QR_question")
                )
                if (question_text_type.equals("music"))
                    it.add_action(
                        ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                        ActionSpeak(context().getString(R.string.action_qcm_answer_vocal_prefix)+num_section),
                        ActionPlayMediaURL(question_text, true)
                    )
                else
                    it.add_action(
                        ActionPrettyPrint(question_text),
                        ActionSpeak(context().getString(R.string.action_qcm_answer_vocal_prefix)+num_section),
                        ActionSpeak(question_text)
                    )
                it.add_action(ActionSpeak(context().getString(R.string.action_qcm_allowed_answers)))
                for((key, value) in answers){
                    it.add_action(
                        ActionSpeak(key),
                        ActionSpeak(value)
                    )
                }
                it.add_action(
                    ActionSpeakBeginnerHelp(
                        MainApplication.application_context()
                            .getString(R.string.action_vocal_beginner_help)
                    ),
                    ActionAddVar(EngineVarInt("QR_question",num_section)),
                    ActionAddVar(EngineVarBool("SR_start", true))
                )
                CoreEngine.add_user_rule(it)
            }

            // Seek to a section left swipe to replay question and double left swipe to go to first question
            EngineRule("Play_seek_${num_section}").let {
                it.add_head_atom(EngineVarInt("seek_section", 0), true)
                it.add_head_atom(EngineVarInt("QR_section", num_section), false)
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
                                && (seek_section_value == -1000))//double swipe left
                                CoreEngine.insert(EngineVarInt("QR_section", 1), { ->
                                    SpeechRecognitionEngine.cancel()
                                    QRDetectorEngine.cancel()
                                    call_back_on_finish()
                                })
                            else
                                if ((SpeechRecognitionEngine.is_recording() || QRDetectorEngine.is_scanning())
                                    &&  (seek_section_value == -2))//simple swipe left
                                    CoreEngine.insert(EngineVarInt("QR_section",  qr_section_value), { ->
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

            // Add a rule to replay question if we abort Speech recognition
            EngineRule("Replay_on_SR_abort_${num_section}").let {
                it.add_head_atom(EngineVarInt("QR_section",num_section),false)
                it.add_head_atom(EngineVarString("SR_abort", ""), true)
                it.add_action(
                    ActionRemoveVar("SR_abort"),
                    ActionAddVar(EngineVarInt("QR_section", num_section)),
                    ActionAddVar(EngineVarBool("Play_next_section", true))
                )
                CoreEngine.add_user_rule(it)
            }

            num_section++
            answers.clear()
        }

        // Add failthrough rule for unknown answer
        EngineRule("Say_unkown_answer").let {
            it.add_head_atom(EngineVarString("SR_text", ""), true)
            it.add_action(
                ActionRemoveVar("SR_text"),
                ActionSpeak(context().getString(R.string.action_qcm_unknown_answer)),
                ActionAddVar(EngineVarBool("SR_start", true))
            )
            CoreEngine.add_user_rule(it)
        }

        // Add failthrough rule not understanding answer
        EngineRule("Say_unrecognize_answer").let {
            it.add_head_atom(EngineVarString("SR_error", ""), true)
            it.add_action(
                ActionRemoveVar("SR_error"),
                ActionSpeak(context().getString(R.string.action_qcm_dont_understand)),
                ActionAddVar(EngineVarBool("SR_start", true))
            )
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to speak the end of the exercice
        EngineRule("Play_section_end").let{
            it.add_head_atom(EngineVarInt("QR_section",num_section),false)
            it.add_head_atom(EngineVarBool("Play_next_section",true),false)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionRemoveVar("QR_section"),
                ActionPrettyPrint(context().getString(R.string.end_QCM)),
                ActionSpeak(context().getString(R.string.end_QCM)),
                ActionAddVar(EngineVarInt("QR_section",num_section+1)),
                ActionAddVar(EngineVarBool("Play_next_section",true))
            )
            CoreEngine.add_user_rule(it)
        }

        ++num_section
        // Add a rule to clear QR code variables
        // and start QR detection
        EngineRule("Play_section_clear").let {
            it.add_head_atom(EngineVarInt("QR_section", num_section), false)
            it.add_head_atom(EngineVarBool("Play_next_section", true), false)
            it.add_action(
                ActionRemoveVar("Play_next_section"),
                ActionRemoveVar("QR_section")
            )
            CoreEngine.add_user_rule(it)
        }


        // Add a rule to stop the media player if running
        EngineRule("Stop_mediaPlayer").let {
            it.add_head_atom(EngineVarInt("seek_section", 0), false)
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

        logger(
            context().getString(R.string.action_new_rules_added) + " : " + CoreEngine.rules_to_string(),
            Logger.DEBUG_LEVEL.INFO
        )

        // Add initial variables
        CoreEngine.insert(EngineVarInt("QR_section", 1))
        CoreEngine.insert(EngineVarBool("Play_next_section", true))
    }
}