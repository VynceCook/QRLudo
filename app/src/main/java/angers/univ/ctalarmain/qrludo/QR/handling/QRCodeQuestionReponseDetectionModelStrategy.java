package angers.univ.ctalarmain.qrludo.QR.handling;

import android.util.Log;

import java.util.Map;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeQuestion;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeReponse;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

import static angers.univ.ctalarmain.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;
import static angers.univ.ctalarmain.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Valentine Rahier
 */
public class QRCodeQuestionReponseDetectionModelStrategy extends QRCodeDetectionModeStrategy {

    private QRCodeQuestion m_question;

    QRCodeQuestionReponseDetectionModelStrategy(MainActivity mainActivity, QRCodeQuestion question) {
        super(mainActivity);
        m_question = question;
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeQuestionReponseDetectionModelStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        if(m_question!=null){
            if(detectedQR instanceof QRCodeReponse){
                QRCodeReponse reponse = (QRCodeReponse)detectedQR;
                if(m_question.getReponses().containsKey(reponse.getId())){
                    m_mainActivity.reponseFind(m_question.getReponses().get(((QRCodeReponse) detectedQR).getId()));
                    //m_mainActivity.stopDetection();
                } else {
                    m_mainActivity.reponseFind("Dommage, ceci n'est pas la bonne réponse");
                    //m_mainActivity.stopDetection();
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
            //Reading again the current QRContent provided at least one QR has been detected.
            m_mainActivity.readCurrentContent();
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
        //The user cannot swipe right in case of question /reponse reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onSwipeRight() {
        //The user cannot swipe right in case of question / reponse reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onLongClick() {

    }
}
