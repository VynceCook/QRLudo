package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.MediaPlayerEngine
import fr.angers.univ.qrludo.engines.QRDetectorEngine
import fr.angers.univ.qrludo.engines.SpeechRecognitionEngine
import fr.angers.univ.qrludo.engines.coreatoms.*
import fr.angers.univ.qrludo.engines.coreatoms.actions.*
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_Open_Question
import fr.angers.univ.qrludo.jsonmodels.JSON_QR_Question_Exercice
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Load a user program (set of rules) in the CoreEngine. The user program
 * changes the behaviour of QRLudo to react (playing sound, launch speech
 * recognition or QR detection) to a QR code type "Vocal Open Question".
 */
/*
------------------------------
Example of generated program:
------------------------------

{ Int:QR_section(1), Bool:Play_next_section(true) }

 _u_<Play_section_1 @ Int:QR_section(1), Play_next_section --> Remove(Play_next_section), Add(Int:QR_section(2)), PrettyPrint(Quel est le prénom de ma femme), Speak(Quel est le prénom de ma femme), SpeakBeginnerHelp(C'est une question à reconnaissance vocale), Add(Bool:SR_start(true)), Add(Bool:Play_next_section(true))>
 _u_<Check_right_answer @ Regex:SR_text[Cécile] --> Remove(SR_text), Speak(Bien joué)>
 _u_<Say_wrong_answer @ SR_text --> Remove(SR_text), Speak(Vous ferez mieux la prochaine fois), Add(Bool:SR_start(true))>
 _u_<Say_unrecognize_text @ SR_error --> Remove(SR_error), Speak(Je n'ai pas reconnu votre réponse), Add(Bool:SR_start(true))>
 _u_<Replay_on_QR_abort @ SR_abort --> Remove(SR_abort), Add(Int:QR_section(1)), Add(Bool:Play_next_section(true))>
 _u_<Play_section_clear @ Int:QR_section(2), Play_next_section --> Remove(Play_next_section), Remove(QR_section)>
 _u_<RePlay_QR @ Int:seek_section(-1000) --> Remove(seek_section), Add(Int:QR_section(1)), Go to first()>
 _u_<Stop_mediaPlayer @ Int:seek_section(0) --> Cancel mediaPlayer()>
 _u_<Play_seek @ seek_section, QR_section --> Remove(seek_section), Update QR_section()>


 _s_<Start_Speech_recognition @ SR_start --> Remove(SR_start), SpeakBeginnerHelp(Parlez après le bip), SpeechRecognition>
 _s_<Start_QR_detection @ QR_start --> Remove(QR_start), SpeakBeginnerHelp(Détection en cours), Tone(START_DETECTION), QRdetect>
 _s_<Analyse_QR_code @ QR_code --> Remove(QR_code), QRAnalyse>
 _s_<Wrong_QR_code_format_version @ Int:QR_code_error(1) --> Remove(QR_code_error), PrettyPrint(Cette version du format de QR code n'est pas prise en compte.), Speak(Cette version du format de QR code n'est pas prise en compte.)>
 _s_<Check_TTS_good @ _CORESYS_QR_initialized --> Speak(QRLudo est lancé)>
 _s_<Check_TTS_good @ _CORESYS_QR_initialized_self_timer, _CORESYS_QR_initialized --> Remove(_CORESYS_QR_initialized_self_timer), Remove(_CORESYS_QR_initialized)>
 _s_<Check_TTS_error @ _CORESYS_QR_initialized_self_timer --> Remove(_CORESYS_QR_initialized_self_timer), PrettyPrint(La synthèse vocale n'est pas initialisée, QRLudo va quitter !), Exit_app()>
 _s_<Clear_asking_for_backup_rules @ ask_for_backup_user_rules --> Remove(ask_for_backup_user_rules)>
 _s_<Clear_ask_for_restore_user_rules @ ask_for_restore_user_rules --> Remove(ask_for_restore_user_rules)>
 _s_<Clear_seek_section @ seek_section --> Remove(seek_section)>
 _s_<Clear_MPE @ MPE_end --> Remove(MPE_end)>
 _s_<Clear_TTS @ TTS_end --> Remove(TTS_end)>
 _s_<Clear_QR_abort @ QR_abort --> Remove(QR_abort)>
 _s_<Clear_SpeechRec_partial @ SR_text_partial --> Remove(SR_text_partial)>
 _s_<Clear_SpeechRec_error @ SR_error --> Remove(SR_error)>
 _s_<Clear_SpeechRec_abort @ SR_abort --> Remove(SR_abort)>
 */
object QR_Vocal_OQ_Program {
    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("LoadRules_QRVocalOpenQuestion", msg, level)
    }

    fun load_from_json(data: String) {
        logger(context().getString(R.string.action_qo_vocale_found), Logger.DEBUG_LEVEL.INFO)

        val gson: Gson = GsonBuilder().create()
        val qe_object : JSON_QR_Open_Question = gson.fromJson(data, JSON_QR_Open_Question::class.java)

        if ((qe_object.name == null) || (qe_object.data == null)
            || (qe_object.text_bonne_reponse == null) || (qe_object.text_mauvaise_reponse == null)) {
            logger(context().getString(R.string.action_qr_code_missing_field), Logger.DEBUG_LEVEL.ERROR)
            return
        }

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
                ActionAddVar(EngineVarInt("QR_section", num_section + 1)))
            if (qe_object.name!!.startsWith("http://") || qe_object.name!!.startsWith("https://"))
                it.add_action(
                    ActionPrettyPrint(context().getString(R.string.action_puzzle_play_media)),
                    ActionPlayMediaURL(qe_object.name!!, true))
            else
                it.add_action(
                    ActionPrettyPrint(qe_object.name!!),
                    ActionSpeak(qe_object.name!!))
            it.add_action(
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.action_vocal_beginner_help)),
                ActionAddVar(EngineVarBool("SR_start",true)),
                ActionAddVar(EngineVarBool("Play_next_section", true) ))
            CoreEngine.add_user_rule(it)
        }

        // We add rules for each good answer
        for (br in qe_object.data!!) {
            EngineRule("Check_right_answer").let {
                it.add_head_atom(EngineVarRegex("SR_text", br), false)
                it.add_action(
                    ActionRemoveVar("SR_text"),
                    ActionSpeak(qe_object.text_bonne_reponse!!))
                CoreEngine.add_user_rule(it)
            }
        }

        // Add failthrough rule for wrong answer
        EngineRule("Say_wrong_answer").let {
            it.add_head_atom(EngineVarString("SR_text", ""), true)
            it.add_action(
                ActionRemoveVar("SR_text"),
                ActionSpeak(qe_object.text_mauvaise_reponse!!),
                ActionAddVar(EngineVarBool("SR_start",true)))
            CoreEngine.add_user_rule(it)
        }

        // Add failthrough rule for text not understood
        EngineRule("Say_unrecognize_text").let {
            it.add_head_atom(EngineVarString("SR_error", ""), true)
            it.add_action(
                ActionRemoveVar("SR_error"),
                ActionSpeak(context().getString(R.string.action_qo_dont_understand)),
                ActionAddVar(EngineVarBool("SR_start",true)))
            CoreEngine.add_user_rule(it)
        }

        // Add a rule to replay question if we abort Speech recognition
        EngineRule("Replay_on_QR_abort").let {
            it.add_head_atom(EngineVarString("SR_abort", ""), true)
            it.add_action(
                ActionRemoveVar("SR_abort"),
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
            it.add_action(
                ActionRemoveVar("seek_section"),
                ActionAddVar(EngineVarInt("QR_section", 1)),
                ActionLambda(
                    "Go to first",
                    { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        // Action is handled only when speech recognition is recording
                        if (SpeechRecognitionEngine.is_recording()) {
                            SpeechRecognitionEngine.cancel()
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

        // Seek to a section left swipe and double left swipe have same effect
        EngineRule("Play_seek").let {
            it.add_head_atom(EngineVarInt("seek_section", 0), true)
            it.add_head_atom(EngineVarInt("QR_section", 0), true)
            it.add_action(ActionRemoveVar("seek_section"),
                ActionLambda(
                    "Update QR_section",
                    { head_var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        var seek_section_value = 0
                        for (v in head_var_list) {
                            if (v._name == "seek_section") seek_section_value =
                                (v as EngineVarInt)._value
                        }
                        // We skip to another section
                        // We manage only seek to previous and seek to current and
                        // action is handled only when speech recognition is recording
                        if ((SpeechRecognitionEngine.is_recording())
                            && (seek_section_value == -1) || (seek_section_value == -2))
                            CoreEngine.insert(EngineVarInt("QR_section", 1), { ->
                                SpeechRecognitionEngine.cancel()
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
        CoreEngine.insert( EngineVarInt("QR_section", 1) )
        CoreEngine.insert( EngineVarBool("Play_next_section", true) )
    }
}