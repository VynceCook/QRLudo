package fr.angers.univ.qrludo.jsonmodels.program_loads

import android.widget.Toast
import fr.angers.univ.qrludo.MainActivity
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.ToneEngine
import fr.angers.univ.qrludo.engines.coreatoms.*
import fr.angers.univ.qrludo.engines.coreatoms.actions.*
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Load QRLudo system program
 *
 * It gathers all default and system actions
 */
/*
------------------------------
Example of generated program:
------------------------------

  { }

 _s_<Start_Speech_recognition @ SR_start --> Remove(SR_start), SpeakBeginnerHelp(Parlez après le bip), Tone(START_DETECTION), SpeechRecognition>
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
object QR_Ludo_Program {

    fun load() {
        Logger.log("MacroCodeLoading", MainApplication.application_context().getString(R.string.macro_code_load), Logger.DEBUG_LEVEL.INFO)
        CoreEngine.clear_system_rules()

        /**
         * SPEECH RECOGNITION
         */
        EngineRule("Start_Speech_recognition").let {
            it.add_head_atom(EngineVarBool("SR_start", true), true)
            it.add_action(
                ActionRemoveVar("SR_start"),
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.spre_talk_after_tone)))
            if (MainApplication.SR_beep_on)
                it.add_action(ActionTone(ToneEngine.TONE_NAME.START_DETECTION))
            it.add_action(ActionSpeechRecognition())
            CoreEngine.add_system_rule(it)
        }

        /**
         * ANALYSE NEW QR CODE
         */
        EngineRule("Start_QR_detection").let {
            it.add_head_atom(EngineVarBool("QR_start", true), true)
            it.add_action(
                ActionRemoveVar("QR_start"),
                ActionSpeakBeginnerHelp(MainApplication.application_context().getString(R.string.beginner_help_new_detection)),
                ActionTone(ToneEngine.TONE_NAME.START_DETECTION),
                ActionQRDetector() )
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Analyse_QR_code").let {
            it.add_head_atom(EngineVarString("QR_code", ""), true)
            it.add_action(ActionRemoveVar("QR_code"), ActionAnalyseQRCode())
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Wrong_QR_code_format_version").let {
            it.add_head_atom(EngineVarInt("QR_code_error", 1), false)
            it.add_action(
                ActionRemoveVar("QR_code_error"),
                ActionPrettyPrint(MainApplication.application_context().getString(R.string.qre_wrong_format_version)),
                ActionSpeak(MainApplication.application_context().getString(R.string.qre_wrong_format_version)))
            CoreEngine.add_system_rule(it)
        }

        /**
         * TEST TTS IS FUNCTIONAL
         */
        EngineRule("Check_TTS_good").let {
            it.add_head_atom(EngineVarBool("_CORESYS_QR_initialized", true), true)
            it.add_action(ActionSpeak(MainApplication.application_context().getString(R.string.core_engine_qrludo_started)))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Check_TTS_good").let {
            it.add_head_atom(EngineVarBool("_CORESYS_QR_initialized_self_timer", true), true)
            it.add_head_atom(EngineVarBool("_CORESYS_QR_initialized", true), true)
            it.add_action(ActionRemoveVar("_CORESYS_QR_initialized_self_timer"),ActionRemoveVar("_CORESYS_QR_initialized"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Check_TTS_error").let {
            it.add_head_atom(EngineVarBool("_CORESYS_QR_initialized_self_timer", true), true)
            it.add_action(
                ActionRemoveVar("_CORESYS_QR_initialized_self_timer"),
                ActionPrettyPrint(MainApplication.application_context().getString(R.string.tts_not_initialised)),
                ActionLambda(
                "Exit_app",
                    { _: MutableList<EngineVar>, call_back_on_finish: () -> Unit ->
                        val activity : MainActivity? = MainApplication.Main_Activity
                        if (activity != null)
                        {
                            Toast.makeText(MainApplication.Main_Activity, MainApplication.application_context().getString(R.string.tts_not_initialised), Toast.LENGTH_LONG).show()
                            activity.exit_application(3000)
                        }
                        call_back_on_finish()
                    }) )
            CoreEngine.add_system_rule(it)
        }

        /**
         * CLEAR REMAINING VARIABLES
         */
        EngineRule("Clear_asking_for_backup_rules").let {
            it.add_head_atom(EngineVarBool("ask_for_backup_user_rules", true), true)
            it.add_action(ActionRemoveVar("ask_for_backup_user_rules"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_ask_for_restore_user_rules").let {
            it.add_head_atom(EngineVarBool("ask_for_restore_user_rules", true), true)
            it.add_action(ActionRemoveVar("ask_for_restore_user_rules"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_seek_section").let {
            it.add_head_atom(EngineVarInt("seek_section", 0), true)
            it.add_action(ActionRemoveVar("seek_section"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_MPE").let {
            it.add_head_atom(EngineVarString("MPE_end", ""), true)
            it.add_action(ActionRemoveVar("MPE_end"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_TTS").let {
            it.add_head_atom(EngineVarString("TTS_end", ""), true)
            it.add_action(ActionRemoveVar("TTS_end"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_QR_abort").let {
            it.add_head_atom(EngineVarString("QR_abort", ""), true)
            it.add_action(ActionRemoveVar("QR_abort"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_SpeechRec_partial").let {
            it.add_head_atom(EngineVarString("SR_text_partial", ""), true)
            it.add_action(ActionRemoveVar("SR_text_partial"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_SpeechRec_error").let {
            it.add_head_atom(EngineVarString("SR_error", ""), true)
            it.add_action(ActionRemoveVar("SR_error"))
            CoreEngine.add_system_rule(it)
        }
        EngineRule("Clear_SpeechRec_abort").let {
            it.add_head_atom(EngineVarString("SR_abort", ""), true)
            it.add_action(ActionRemoveVar("SR_abort"))
            CoreEngine.add_system_rule(it)
        }

    }
}