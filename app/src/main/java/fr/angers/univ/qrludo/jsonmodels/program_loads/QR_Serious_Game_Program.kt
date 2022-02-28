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

{ Int:QR_section(1), Bool:Play_next_section(true) }

_u_<Play_section_1 @ Int:QR_section(1), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Bienvenue dans le Serious Game. Voici la première question.), Speak(Bienvenue dans le Serious Game. Voici la première question.), Add(Int:QR_section(6)), Add(Bool:Play_next_section(true))>
_u_<Play_seek_1 @ seek_section, Int:QR_section(1) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Play_section_2 @ Int:QR_section(2), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Mauvaise réponse, essaie encore), Speak(Mauvaise réponse, essaie encore), Add(Int:QR_section(7)), Add(Bool:Play_next_section(true))>
_u_<Play_seek_2 @ seek_section, Int:QR_section(2) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Play_section_3 @ Int:QR_section(3), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Mauvaise réponse, essaie encore), Speak(Mauvaise réponse, essaie encore), Add(Int:QR_section(6)), Add(Bool:Play_next_section(true))>
_u_<Play_seek_3 @ seek_section, Int:QR_section(3) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Play_section_4 @ Int:QR_section(4), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Bravo, vous avez fini l'exercice), Speak(Bravo, vous avez fini l'exercice), Add(Int:QR_section(9)), Add(Bool:Play_next_section(true))>
_u_<Play_seek_4 @ seek_section, Int:QR_section(4) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Play_section_5 @ Int:QR_section(5), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Mauvaise réponse, essaie encore), Speak(Mauvaise réponse, essaie encore), Add(Int:QR_section(8)), Add(Bool:Play_next_section(true))>
_u_<Play_seek_5 @ seek_section, Int:QR_section(5) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Check_right_answer @ Regex:SR_text[Angers], Int:QR_question(6) --> Remove(SR_text), Add(Int:QR_section(3)), Add(Bool:Play_next_section(true))>
_u_<Check_right_answer @ Regex:SR_text[Paris], Int:QR_question(6) --> Remove(SR_text), Add(Int:QR_section(7)), Add(Bool:Play_next_section(true))>
_u_<Check_right_answer @ Regex:SR_text[Marseille], Int:QR_question(6) --> Remove(SR_text), Add(Int:QR_section(3)), Add(Bool:Play_next_section(true))>
_u_<Play_section_6 @ Int:QR_section(6), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Quelle est la capitale de la France ?), Speak(Quelle est la capitale de la France ?), Speak(Les réponses possibles sont), Speak(Angers), Speak(Paris), Speak(Marseille), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Int:QR_question(6)), Add(Bool:SR_start(true))>
_u_<Play_seek_6 @ seek_section, Int:QR_section(6) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Check_wrong_answer @ Regex:SR_text[(^((?!Mémoire sémantique).)*$((?!Mémoire des connaissances).)*$)], Int:QR_question(7) --> Remove(SR_text), Add(Int:QR_section(2)), Add(Bool:Play_next_section(true))>
_u_<Check_right_answer @ Regex:SR_text[Mémoire sémantique], Int:QR_question(7) --> Remove(SR_text), Add(Int:QR_section(8)), Add(Bool:Play_next_section(true))>
_u_<Check_right_answer @ Regex:SR_text[Mémoire des connaissances], Int:QR_question(7) --> Remove(SR_text), Add(Int:QR_section(8)), Add(Bool:Play_next_section(true))>
_u_<Play_section_7 @ Int:QR_section(7), Play_next_section --> Remove(Play_next_section), Remove(QR_question), PrettyPrint(Comment s'appelle la mémoire dans laquelle sont stockés des éléments qui nous sont propres, nos événements vécus, etc), Speak(Comment s'appelle la mémoire dans laquelle sont stockés des éléments qui nous sont propres, nos événements vécus, etc), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Int:QR_question(7)), Add(Bool:SR_start(true))>
_u_<Play_seek_7 @ seek_section, Int:QR_section(7) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Check_qr_answer @ String:QR_answer(88b07d8ec79bc9b192ffb0dd765b4721) --> Remove(QR_answer), Add(Int:QR_section(5)), Add(Bool:Play_next_section(true))>
_u_<Check_qr_answer @ String:QR_answer(f21d85d0a0e4e7f18e3bd3a195573289) --> Remove(QR_answer), Add(Int:QR_section(4)), Add(Bool:Play_next_section(true))>
_u_<Check_qr_answer @ String:QR_answer(4bf416a6b928925f3b0a0db30813f861) --> Remove(QR_answer), Add(Int:QR_section(4)), Add(Bool:Play_next_section(true))>
_u_<Play_section_8 @ Int:QR_section(8), Play_next_section --> Remove(Play_next_section), PrettyPrint(Comment s'appelle la mémoire dans laquelle sont stockés des éléments qui nous sont propres, nos événements vécus, etc), Speak(Comment s'appelle la mémoire dans laquelle sont stockés des éléments qui nous sont propres, nos événements vécus, etc), SpeakBeginnerHelp(Un double balayage rapide vers le haut vous permet de basculer entre le mode exploration et le mode de réponse à la question), Add(Bool:QR_start(true)), Add(Bool:Play_next_section(true))>
_u_<Play_seek_8 @ seek_section, Int:QR_section(8) --> Remove(seek_section), Update QR_section(), Add(Bool:Play_next_section(true))>
_u_<Say_unkown_answer @ SR_text --> Remove(SR_text), Speak(Votre réponse ne fait pas partie de la liste des réponses possibles), Add(Bool:SR_start(true))>
_u_<Say_unrecognize_answer @ SR_error --> Remove(SR_error), Speak(Je n'ai pas reconnu votre réponse), Add(Bool:SR_start(true))>
_u_<Analyse_QR_answer @ QR_code --> Remove(QR_code), Extract QR ID()>
_u_<Go_to_exploration_mode @ ask_for_backup_user_rules --> Remove(ask_for_backup_user_rules), PrettyPrint(Vous etes en mode exploration), Speak(Vous etes en mode exploration), SpeakBeginnerHelp(Un double balayage rapide vers le haut vous permet de basculer entre le mode exploration et le mode de réponse à la question), Backup user rules and variables(), Add(Bool:QR_start(true))>
_u_<Go_to_answer_mode @ ask_for_restore_user_rules --> Remove(ask_for_restore_user_rules), PrettyPrint(Vous etes en mode de réponse à la question), Speak(Vous etes en mode de réponse à la question), SpeakBeginnerHelp(Un double balayage rapide vers le haut vous permet de basculer entre le mode exploration et le mode de réponse à la question), Add(Bool:QR_start(true))>
_u_<Replay_on_QR_abort @ QR_abort --> Remove(QR_abort), Add(Int:QR_section(1)), Add(Bool:Play_next_section(true))>
_u_<Play_section_clear @ Int:QR_section(9), Bool:Play_next_section(true) --> Remove(Play_next_section), Remove(QR_section)>
_u_<Stop_mediaPlayer @ Int:seek_section(0) --> Cancel mediaPlayer()>

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

    // Class to save answers
    class Answer(var answer: String?, var next: String?)

    // Class to save all labels and questions
    class Section_label_question(var id:Int, var name:String, var text:String?, var answers: ArrayList<Answer>, var read_type:String, var question_type:String?, var type_node:String)

    fun load_from_json(data: String) {
        logger(context().getString(R.string.action_serious_game_found), Logger.DEBUG_LEVEL.INFO)

        val gson: Gson = GsonBuilder().create()
        val tree_json_object = gson.fromJson(data, JSON_QR_Serious_Game::class.java)
        val list_label_questions = mutableListOf<Section_label_question>()
        val list_label_json = tree_json_object.label_nodes!!
        val list_question_json = tree_json_object.question_nodes!!
        var id_section = 1

        // Clear all previous user rules
        CoreEngine.clear_user_rules()
        CoreEngine.clear_user_var_store()

        // Add on data list all labels
        for(label in list_label_json){
            val label_object = label.asJsonObject
            val name_label_node = gson.fromJson(label_object.getAsJsonPrimitive("name"), String::class.java)
            val label_question_object = gson.fromJson(label_object.getAsJsonObject("txt"),JsonObject::class.java)
            val label_type = gson.fromJson(label_question_object.getAsJsonPrimitive("type"),String::class.java)
            var label_text:String
            val label_next = gson.fromJson(label_object.getAsJsonPrimitive("ext"),String::class.java)
            val type_node = "LabelNode"

            // if label is a type music
            if(label_type == "M")
                label_text = gson.fromJson(label_question_object.getAsJsonPrimitive("url"),String::class.java)
            // type text
            else
                label_text = gson.fromJson(label_question_object.getAsJsonPrimitive("txt"),String::class.java)

            val answers = ArrayList<Answer>()
            if(label_next == "")
                answers.add(Answer(null, null))
            else
                answers.add(Answer(null, label_next))
            val textObject = Section_label_question(id_section, name_label_node, label_text, answers, label_type,null, type_node)
            list_label_questions.add(textObject)

            id_section++
        }

        // Add on data list all questions
        for(question in list_question_json){
            val question_object = question.asJsonObject
            val name_question_node = gson.fromJson(question_object.getAsJsonPrimitive("name"), String::class.java)
            val text_question_object = gson.fromJson(question_object.getAsJsonObject("txt"), JsonObject::class.java)
            val text_question_type = gson.fromJson(text_question_object.getAsJsonPrimitive("type"), String::class.java)
            val question_type = gson.fromJson(question_object.getAsJsonPrimitive("type"), String::class.java)
            var text_question:String
            val answer_array=question_object.getAsJsonArray("rep")
            val type_node = "QuestionNode"

            if(text_question_type.equals("M"))
                text_question = gson.fromJson(text_question_object.getAsJsonPrimitive("url"),String::class.java)
            else
                text_question = gson.fromJson(text_question_object.getAsJsonPrimitive("txt"),String::class.java)

            val answers = ArrayList<Answer>()
            for(answer in answer_array){
                val answer_object = answer.asJsonObject
                val answer_text = gson.fromJson(answer_object.getAsJsonPrimitive("txt"),String::class.java)
                val answer_next = gson.fromJson(answer_object.getAsJsonPrimitive("ext"),String::class.java)
                answers.add(Answer(answer_text, answer_next))
            }
            val textObject = Section_label_question(id_section,name_question_node,text_question,answers,text_question_type, question_type, type_node)
            list_label_questions.add(textObject)
            id_section++
        }

        // Add all rules to read the tree
        for(text_question_obj in list_label_questions){
            val section_id = text_question_obj.id
            val section_name = text_question_obj.name
            val section_text = text_question_obj.text
            val answers = text_question_obj.answers
            val read_type = text_question_obj.read_type
            val question_type = text_question_obj.question_type
            val type_node = text_question_obj.type_node

            var str = section_name
            for (answer in answers){
                str += " ${answer.answer}"
            }

            // Add rules for question nodes
            if(type_node == "QuestionNode"){
                when(question_type){
                    // Type QCM
                    "M" -> {
                        for(answer in answers){
                            val next = next_section(answer.next!!,list_label_questions)

                            // Add rule to play next section if it's an answer for this question
                            EngineRule("Check_right_answer").let {
                                it.add_head_atom(EngineVarRegex("SR_text", answer.answer!!), false)
                                it.add_head_atom(EngineVarInt("QR_question", section_id), false)
                                it.add_action(ActionRemoveVar("SR_text"))
                                it.add_action(
                                    ActionAddVar(EngineVarInt("QR_section", next)),
                                    ActionAddVar(EngineVarBool("Play_next_section", true))
                                )
                                CoreEngine.add_user_rule(it)
                            }
                        }

                        // Add rule to speak question and start vocal recognition
                        EngineRule("Play_section_$section_id").let {
                            it.add_head_atom(EngineVarInt("QR_section", section_id), false)
                            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                            it.add_action(
                                ActionRemoveVar("Play_next_section"),
                                ActionRemoveVar("QR_question")
                            )
                            if (read_type == "M")
                                it.add_action(
                                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                                    ActionPlayMediaURL(section_text!!, true)
                                )
                            else
                                it.add_action(
                                    ActionPrettyPrint(section_text!!),
                                    ActionSpeak(section_text)
                                )
                            it.add_action(ActionSpeak(context().getString(R.string.action_qcm_allowed_answers)))
                            for (answer in answers) {
                                it.add_action(ActionSpeak(answer.answer!!))
                            }
                            it.add_action(
                                ActionSpeakBeginnerHelp(
                                    MainApplication.application_context()
                                        .getString(R.string.action_vocal_beginner_help)
                                ),
                                ActionAddVar(EngineVarInt("QR_question", section_id)),
                                ActionAddVar(EngineVarBool("SR_start", true))
                            )
                            CoreEngine.add_user_rule(it)
                        }

                    }
                    // Type open question
                    "O" -> {
                        for(answer in answers) {
                            val next = next_section(answer.next!!,list_label_questions)

                            if (answer.answer != "Autre*") {
                                EngineRule("Check_right_answer").let {
                                    it.add_head_atom(EngineVarRegex("SR_text", answer.answer!!), false)
                                    it.add_head_atom(EngineVarInt("QR_question", section_id), false)
                                    it.add_action(ActionRemoveVar("SR_text"))
                                    it.add_action(
                                        ActionAddVar(EngineVarInt("QR_section", next)),
                                        ActionAddVar(EngineVarBool("Play_next_section", true))
                                    )
                                    CoreEngine.add_user_rule(it)
                                }
                            } else {
                                // We make a regex to match with any answer not containing good answer of the question
                                val regex = get_regex_QO(section_name, list_label_questions)
                                EngineRule("Check_wrong_answer").let {
                                    it.add_head_atom(EngineVarRegex("SR_text", regex), false)
                                    it.add_head_atom(EngineVarInt("QR_question", section_id), false)
                                    it.add_action(ActionRemoveVar("SR_text"))
                                    it.add_action(
                                        ActionAddVar(EngineVarInt("QR_section", next)),
                                        ActionAddVar(EngineVarBool("Play_next_section", true))
                                    )
                                    CoreEngine.add_user_rule(it)
                                }
                            }
                        }

                        // Add rule to speak question and start vocal recognition
                        EngineRule("Play_section_$section_id").let {
                            it.add_head_atom(EngineVarInt("QR_section", section_id), false)
                            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                            it.add_action(
                                ActionRemoveVar("Play_next_section"),
                                ActionRemoveVar("QR_question")
                            )
                            if (read_type == "M")
                                it.add_action(
                                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                                    ActionPlayMediaURL(section_text!!, true)
                                )
                            else
                                it.add_action(
                                    ActionPrettyPrint(section_text!!),
                                    ActionSpeak(section_text)
                                )
                            it.add_action(
                                ActionSpeakBeginnerHelp(
                                    MainApplication.application_context()
                                        .getString(R.string.action_vocal_beginner_help)
                                ),
                                ActionAddVar(EngineVarInt("QR_question", section_id)),
                                ActionAddVar(EngineVarBool("SR_start", true))
                            )
                            CoreEngine.add_user_rule(it)
                        }
                    }
                    // Type QR Code question
                    "Q" -> {
                        for(answer in answers) {
                            val next = next_section(answer.next!!,list_label_questions)

                            // Add rule to play next section if it's an answer for this question
                            EngineRule("Check_qr_answer").let {
                                it.add_head_atom(EngineVarString("QR_answer", answer.answer!!), false)
                                it.add_action(ActionRemoveVar("QR_answer"))
                                it.add_action(
                                    ActionAddVar(EngineVarInt("QR_section", next)),
                                    ActionAddVar(EngineVarBool("Play_next_section", true))
                                )
                                CoreEngine.add_user_rule(it)
                            }
                        }

                        // Add rule to speak question
                        EngineRule("Play_section_$section_id").let {
                            it.add_head_atom(EngineVarInt("QR_section", section_id), false)
                            it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                            it.add_action(ActionRemoveVar("Play_next_section"))

                            if (read_type == "M")
                                it.add_action(
                                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                                    ActionPlayMediaURL(section_text!!, true)
                                )
                            else
                                it.add_action(
                                    ActionPrettyPrint(section_text!!),
                                    ActionSpeak(section_text)
                                )
                            it.add_action(
                                ActionSpeakBeginnerHelp(
                                    MainApplication.application_context()
                                        .getString(R.string.action_question_exercice_mode_help)
                                ),
                                ActionAddVar(EngineVarBool("QR_start", true)),
                                ActionAddVar(EngineVarBool("Play_next_section", true))
                            )
                            CoreEngine.add_user_rule(it)
                        }
                    }
                }
            } else { // Add rules for label nodes
                // Add rule to speak text and go to the the next section
                EngineRule("Play_section_$section_id").let {
                    it.add_head_atom(EngineVarInt("QR_section", section_id), false)
                    it.add_head_atom(EngineVarBool("Play_next_section", true), true)
                    it.add_action(
                        ActionRemoveVar("Play_next_section"),
                        ActionRemoveVar("QR_question")
                    )
                    if (read_type == "M")
                        it.add_action(
                            ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                            ActionPlayMediaURL(section_text!!, true)
                        )
                    else
                        it.add_action(
                            ActionPrettyPrint(section_text!!),
                            ActionSpeak(section_text )
                        )
                    if(search_end(list_label_questions) == section_id){
                        it.add_action(
                            ActionAddVar(EngineVarInt("QR_section", list_label_questions.size+1)),
                            ActionAddVar(EngineVarBool("Play_next_section", true))
                        )
                    }
                    else {
                        it.add_action(
                            ActionAddVar(EngineVarInt("QR_section", next_section(answers[0].next!!, list_label_questions))),
                            ActionAddVar(EngineVarBool("Play_next_section", true))
                        )
                    }
                    CoreEngine.add_user_rule(it)
                }
            }

            // Seek to a section left swipe to replay question and double left swipe to go to first question
            EngineRule("Play_seek_${section_id}").let {
                it.add_head_atom(EngineVarInt("seek_section", 0), true)
                it.add_head_atom(EngineVarInt("QR_section", section_id), false)
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
                                && (seek_section_value == -1000)) //double swipe left
                                CoreEngine.insert(EngineVarInt("QR_section", search_first(list_label_questions)), {
                                    SpeechRecognitionEngine.cancel()
                                    QRDetectorEngine.cancel()
                                    call_back_on_finish()
                                })
                            else
                                if ((SpeechRecognitionEngine.is_recording() || QRDetectorEngine.is_scanning())
                                    &&  (seek_section_value == -2)) //simple swipe left
                                    CoreEngine.insert(EngineVarInt("QR_section",  qr_section_value), {
                                        SpeechRecognitionEngine.cancel()
                                        QRDetectorEngine.cancel()
                                        call_back_on_finish()
                                    })
                                else
                                    if ((SpeechRecognitionEngine.is_recording() || QRDetectorEngine.is_scanning())
                                        &&  (seek_section_value == 1000)) //double swipe right
                                        CoreEngine.insert(EngineVarInt("QR_section", search_end(list_label_questions)), {
                                            SpeechRecognitionEngine.cancel()
                                            QRDetectorEngine.cancel()
                                            call_back_on_finish()
                                        })
                                    else
                                        call_back_on_finish()
                        }
                    ),
                    ActionAddVar(EngineVarBool("Play_next_section", true)))
                CoreEngine.add_user_rule(it)
            }
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
                            logger(context().getString(R.string.action_qranalyse_decompress) + " : $data_qr_code",Logger.DEBUG_LEVEL.VERBOSE)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    if (data_qr_code.startsWith("{")) {
                        val gson2: Gson = GsonBuilder().create()
                        val converted_json_object: JSON_QR =
                            gson2.fromJson(data_qr_code, JSON_QR::class.java)

                        CoreEngine.insert(EngineVarString("QR_answer",converted_json_object.id.toString()), call_back_on_finish)
                    } else
                        call_back_on_finish()
                })
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to go to exploration mode
        EngineRule("Go_to_exploration_mode").let {
            it.add_head_atom(EngineVarBool("ask_for_backup_user_rules", true), true)
            it.add_action(
                ActionLambda(
                    "Cancel QRDetector and SpeechRecognition",
                    { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        if(QRDetectorEngine.is_scanning())
                            QRDetectorEngine.cancel()
                        if(SpeechRecognitionEngine.is_recording())
                            SpeechRecognitionEngine.cancel()
                        call_back_on_finish()
                    }
                ),
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
                ActionLambda(
                    "Cancel QRDetector and SpeechRecognition",
                    { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        if(QRDetectorEngine.is_scanning())
                            QRDetectorEngine.cancel()
                        if(SpeechRecognitionEngine.is_recording())
                            SpeechRecognitionEngine.cancel()
                        call_back_on_finish()
                    }
                ),
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
                ActionAddVar(EngineVarInt("QR_section", search_first(list_label_questions))),
                ActionAddVar(EngineVarBool("Play_next_section", true))
            )
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to clear QR code variables
        // and start QR detection
        EngineRule("Play_section_clear").let {
            it.add_head_atom(EngineVarInt("QR_section", list_label_questions.size+1), false)
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

        logger(context().getString(R.string.action_new_rules_added) + " : " + CoreEngine.rules_to_string_pretty_print(),
            Logger.DEBUG_LEVEL.INFO
        )

        // Add initial variables
        CoreEngine.insert(EngineVarInt("QR_section", search_first(list_label_questions)))
        CoreEngine.insert(EngineVarBool("Play_next_section", true))

    }

    // Return the name of the first node of the tree
    private fun search_first(listS:MutableList<Section_label_question>):Int{
        var res=0
        for(sect in listS){
            var notTo=false
            for(sect2 in listS){
                for(answer in sect2.answers){
                    if(sect.name == answer.next)
                        notTo=true
                }
            }
            if(!notTo)
                res=sect.id
        }
        return res
    }

    // Return the last node of the tree
    private fun search_end(listS:MutableList<Section_label_question>):Int{
        for(sect in listS){
            for(answer in sect.answers){
                if(answer.next == null)
                    return sect.id
            }
        }
        return 0
    }

    // Return the id of the next section of that pass in parameter or 0 if doesn't exist
    private fun next_section(next:String, listS:MutableList<Section_label_question>):Int{
        for(sect in listS){
            if (sect.name == next)
                return sect.id
        }
        return 0
    }

    // Return a regex for wrong answer in open question
    private fun get_regex_QO(name: String, listS: MutableList<Section_label_question>):String {
        var regex = "(^"
        for(sect in listS){
            if(sect.name == name) {
                for(answer in sect.answers){
                    if(!answer.answer.equals("Autre*"))
                        regex += "((?!${answer.answer}).)*$"
                }
            }
        }
        regex += ")"
        return regex
    }
}