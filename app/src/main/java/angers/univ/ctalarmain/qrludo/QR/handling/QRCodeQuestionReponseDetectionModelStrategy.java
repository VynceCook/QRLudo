package angers.univ.ctalarmain.qrludo.QR.handling;

import android.util.Log;

import java.util.Map;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeQuestion;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeReponse;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

import static angers.univ.ctalarmain.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;

/**
 * Valentine Rahier
 */
public class QRCodeQuestionReponseDetectionModelStrategy extends QRCodeDetectionModeStrategy {

    private QRCodeQuestion m_question;

    QRCodeQuestionReponseDetectionModelStrategy(MainActivity mainActivity, QRCodeQuestion question) {
        super(mainActivity);
        m_question = question;

        if(m_question!=null){
            m_mainActivity.nextDetectionQuestionReponse("Veuillez scanner la bonne réponse");
            hand.removeCallbacks(runner);
        }
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
                    m_mainActivity.stopDetection();
                    m_mainActivity.reponseFind(m_question.getReponses().get(((QRCodeReponse) detectedQR).getId()));
                } else {
                    m_mainActivity.stopDetection();
                    m_mainActivity.reponseFausse();
                }
            }
        } else {
            if(detectedQR instanceof QRCodeQuestion){
                if(!posted) {
                    posted = hand.postDelayed(runner, 1000);
                }else{
                    m_mainActivity.nextDetectionQuestionReponse("Veuillez scanner la bonne réponse");
                    hand.removeCallbacks(runner);
                    posted = false;
                }
            }
        }
    }

    public void readContentQRCode(QRCode detectedQR){
        //Building QR and adding it to the detected QRCodes
        m_detectedQRCodes.addQR(detectedQR);

        //New contentState of the activity
        m_mainActivity.setDetectionProgress(MULTIPLE_QR_DETECTED);

        //Resetting the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();
    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

    }

    @Override
    public void onQRFileDownloadComplete() {

    }

    @Override
    public void onSwipeTop() {
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
    public void onSwipeBottom() {
        onSwipeTop();
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
}
