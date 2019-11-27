package fr.angers.univ.qrludo.QR.handling;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeAtomique;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestion;
import fr.angers.univ.qrludo.QR.model.QRCodeReponse;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Valentine Rahier
 */
public class QRCodeQuestionReponseDetectionModelStrategy extends QRCodeDetectionModeStrategy {

    private QRCodeQuestion m_question;
    private boolean scan_reponse;
    private String textQrCodePrecedent = "";
    private boolean mode_exploration = false;
    private boolean mode_reponse = false;

    QRCodeQuestionReponseDetectionModelStrategy(MainActivity mainActivity, QRCodeQuestion question) {
        super(mainActivity);
        m_question = question;
        scan_reponse = false;
        if(m_question!=null) {
            m_mainActivity.modeExploration(m_question.getQuestionText());
            mode_exploration = true;
        }
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeQuestionReponseDetectionModelStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {

        if(mode_exploration) {
            if (m_question != null) {
                if ((detectedQR instanceof QRCodeAtomique)) {
                    QRCodeAtomique reponse = (QRCodeAtomique) detectedQR;

                    if (!textQrCodePrecedent.equals(reponse.getM_reponse())) {
                        m_mainActivity.readReponse(reponse.getM_reponse());
                        textQrCodePrecedent = reponse.getM_reponse();
                    }
                }
            }
        }

        if(mode_reponse){
            Log.i("================","mode reponse");
            if (m_question != null) {
                if ((detectedQR instanceof QRCodeAtomique)) {
                    QRCodeAtomique reponse = (QRCodeAtomique) detectedQR;

                    if(m_question.getListe_bonne_rep().contains(reponse.getM_id())) {
                        m_mainActivity.readQuestion(m_question.getM_text_bonne_rep());
                    }
                    else
                        m_mainActivity.readQuestion(m_question.getM_text_mauvaise_rep());
                }
            }
        }
    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

    }

    @Override
    public void onQRFileDownloadComplete() {

    }

    @Override
    public void onSwipeTop() {
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
            if(m_question != null){
                m_mainActivity.readQuestion(m_question.getQuestionText());
            }
        }
        else{
            //Signaling that the user cannot swipe top
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeBottom() {
        //Canceling current detection or reading, and starting new detection, provided the tts is ready
        if (m_mainActivity.isTTSReady()) {
            if(!posted) {
                posted = hand.postDelayed(runner, 1000);
            }else{
                m_mainActivity.startNewDetection("Nouvelle détection");
                hand.removeCallbacks(runner);
                posted = false;
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeLeft() {
        scan_reponse = true;
        m_mainActivity.readQuestion("Détection de la réponse");
        mode_reponse = true;
        mode_exploration = false;
    }

    @Override
    public void onSwipeRight() {
        m_mainActivity.readQuestion("Mode exploration");
        mode_reponse = false;
        mode_exploration = true;
    }

    @Override
    public void onDoubleClick() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }
}
