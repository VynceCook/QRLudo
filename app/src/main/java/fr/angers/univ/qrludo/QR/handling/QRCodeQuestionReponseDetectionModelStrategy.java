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

    QRCodeQuestionReponseDetectionModelStrategy(MainActivity mainActivity, QRCodeQuestion question) {
        super(mainActivity);
        m_question = question;
        scan_reponse = false;
        if(m_question!=null) {
            m_mainActivity.modeExploration(m_question.getQuestionText());
        }
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeQuestionReponseDetectionModelStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        //m_mainActivity.stopDetection();

        scan_reponse = true;

        if(m_question!=null){
            if((detectedQR instanceof QRCodeAtomique) && scan_reponse) {
                QRCodeAtomique reponse = (QRCodeAtomique) detectedQR;

                m_mainActivity.reponseFind(reponse.getM_reponse());

            }


            /*if(m_question.getListe_bonne_rep().contains(reponse.getId())){
                m_mainActivity.reponseFind(m_question.getListe_bonne_rep().get(m_question.getM_text_bonne_rep()));
                //m_mainActivity.stopDetection();
            } else {
                m_mainActivity.reponseFind("Dommage, ceci n'est pas la bonne réponse");
                //m_mainActivity.stopDetection();
            }*/
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
    }

    @Override
    public void onSwipeRight() {
        //The user cannot swipe right in case of question / reponse reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onDoubleClick() {
        
    }
}
