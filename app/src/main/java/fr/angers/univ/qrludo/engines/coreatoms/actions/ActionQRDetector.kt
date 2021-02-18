package fr.angers.univ.qrludo.engines.coreatoms.actions

import android.util.Log
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.engines.CoreEngine
import fr.angers.univ.qrludo.engines.QRDetectorEngine
import fr.angers.univ.qrludo.engines.coreatoms.EngineVar
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarBool
import fr.angers.univ.qrludo.engines.coreatoms.EngineVarString
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * Action which starts the QR detection engine. Some variables are posted in the store of
 * variables of the CoreEngine depending on the recognized text
 */
class ActionQRDetector : EngineAction {
    override val _name: String = "QRdetect"

    override fun to_string(): String {
        return super.to_string()
    }

    override fun execute(var_list: MutableList<EngineVar>, call_back_on_finish: () -> Unit) {
        QRDetectorEngine.start(
            { s: String -> CoreEngine.insert(EngineVarString("QR_code", s), call_back_on_finish) },
            {  -> CoreEngine.insert( EngineVarBool("QR_abort", true), call_back_on_finish) })
    }
}