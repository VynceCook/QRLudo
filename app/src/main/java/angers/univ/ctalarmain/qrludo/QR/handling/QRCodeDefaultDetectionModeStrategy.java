package angers.univ.ctalarmain.qrludo.QR.handling;


import android.util.Log;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeEnsemble;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeQuestion;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeReponse;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

import static angers.univ.ctalarmain.qrludo.activities.MainActivity.FIRST_QR_DETECTED;
import static angers.univ.ctalarmain.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;
import static angers.univ.ctalarmain.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Created by Jules Leguy on 29/01/18.
 *
 * Default detecting strategy used while no special QRCode has been detected
 *
 * If a QRCodeEnsemble is detected and is the first of the current detection, launching QREnsembleDetectionStrategy by calling
 * startEnsembleDetection(). Otherwise, ignoring it.
 */
public class QRCodeDefaultDetectionModeStrategy extends QRCodeDetectionModeStrategy {



    public QRCodeDefaultDetectionModeStrategy(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        //Applies a family or ensemble related behaviour if necessary or launches the reading of the detected QR Code
        if (!ensembleBehaviour(detectedQR, true) && !questionReponseBehaviour(detectedQR, true)){

            //adding it to the detected QRCodes
            m_detectedQRCodes.addQR(detectedQR);

            //Changing curent detection state
            m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);

            //Reading the QR
            m_mainActivity.singleReading();

            //Launching the MultipleDetectionTimer. At its end, if other QRCodes have been detected, stopping detection
            m_mainActivity.startMultipleDetectionTimer();

            ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();
        }

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        //Applies a family or ensemble related behaviour if necessary or records the detected QR Code
        if (!ensembleBehaviour(detectedQR, false) && !(questionReponseBehaviour(detectedQR, false))){

            //Building QR and adding it to the detected QRCodes
            m_detectedQRCodes.addQR(detectedQR);

            //New contentState of the activity
            m_mainActivity.setDetectionProgress(MULTIPLE_QR_DETECTED);

            //Resetting the MultipleDetectionTimer
            m_mainActivity.startMultipleDetectionTimer();

            ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();
        }

    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

        //stopping detection
        m_mainActivity.stopDetection();

        //Launching classic multiple reading (the first has already been printed/said, adding only the others
        m_mainActivity.classicMultipleReading();
    }


    @Override
    public void onQRFileDownloadComplete() {
        //plays the newly downloaded sound
        m_mainActivity.playCurrentSoundContent("Fichier audio");
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

        //Can only swipe left if at least one QR has been printed/detected (equivalent in the default detection mode)
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){

            //If the application is still detecting and the user has already reached the last currently available QRContent, cannot swipe left
            if (!(m_mainActivity.isApplicationDetecting() && m_mainActivity.getCurrentPos()==m_mainActivity.getContentSize()-1)){

                //If the app is waiting to be notified by the current QRFile of the end of its downloading, unregister as listener
                m_mainActivity.unregisterToQRFile();

                if (m_mainActivity.getCurrentPos()==m_mainActivity.getContentSize()-1){
                    //Ending the reading if the user had already reached the last QRContent
                    m_mainActivity.startNewDetection("");
                }
                else{
                    //Reading the next QRContent
                    m_mainActivity.incrementCurrentPos();
                    m_mainActivity.readCurrentContent();

                    if (m_mainActivity.getCurrentPos()==m_mainActivity.getContentSize()-1){
                        //Notifying the user if he has just reached the last QRContent
                        ToneGeneratorSingleton.getInstance().lastQRCodeReadTone();
                    }
                }
            }
            else{
                ToneGeneratorSingleton.getInstance().errorTone();
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeRight() {

        //Can only swipe right if at least one QR has been printed/detected (equivalent in the default detection mode)
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){

            if (m_mainActivity.getCurrentPos()==0){
                //Notifying the user that he cannot swipe right because the current QRContent is the first
                ToneGeneratorSingleton.getInstance().firstQRCodeReadTone();
            }
            else{
                //If the app is waiting to be notified by the current QRFile of the end of its downloading, unregister as listener
                m_mainActivity.unregisterToQRFile();

                //Reading the previous QRContent
                m_mainActivity.decrementCurrentPos();
                m_mainActivity.readCurrentContent();
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }

    }

    @Override
    public void onLongClick() {
        m_mainActivity.pauseCurrentReading();
    }

    @Override
    public void onDoubleClick() {
        m_mainActivity.pauseCurrentReading();
    }

    /**
     * Starts a ensemble detection of ignores the QRCodeEnsemble if necessary
     * If no action has been executed, returns false
     *
     * @param detectedQR
     * @param isFirstQRDetected
     * @return
     */
    private boolean ensembleBehaviour(QRCode detectedQR, boolean isFirstQRDetected){

        //Checking if the detected QRCode is a QRCodeEnsemble
        if (detectedQR instanceof QRCodeEnsemble){

            if (isFirstQRDetected){
                startEnsembleDetection(detectedQR);
                return true;
            }
            else{
                //signaling the error and ignoring the QRCode
                ToneGeneratorSingleton.getInstance().ignoredQRCodeTone();
                m_detectedQRCodes.addIgnoredQR(detectedQR);
                return true;
            }

        }
        else{
            Log.v("test", "no ensemble behaviour");
            return false;
        }

    }

    /**
     * Starts a question / reponse detection of ignores the QRCodeQuestion / Reponse if necessary
     * If no action has been executed, returns false
     *
     * @param detectedQR
     * @param isFirstQRDetected
     * @return
     */
    private boolean questionReponseBehaviour(QRCode detectedQR, boolean isFirstQRDetected){

        //Checking if the detected QRCode is a QRCodeEnsemble
        if ((detectedQR instanceof QRCodeQuestion)){

            if (isFirstQRDetected){
                startQuestionReponseDetection(detectedQR);
                return true;
            }
            else{
                //signaling the error and ignoring the QRCode
                ToneGeneratorSingleton.getInstance().ignoredQRCodeTone();
                m_detectedQRCodes.addIgnoredQR(detectedQR);
                return true;
            }

        }
        else {
            Log.v("test", "no question reponse behaviour");
            return false;
        }

    }


    /**
     * Starting a detection of QRCodeEnsemble
     *
     * @param detectedQR
     */
    private void startEnsembleDetection(QRCode detectedQR){

        //Ensemble detection tone
        ToneGeneratorSingleton.getInstance().ensembleDetectionTone();

        //Adding the QRCode to the detected ones
        m_detectedQRCodes.addQR(detectedQR);

        //Changing current detection state
        m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);

        //Launching the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        //Changing detection strategy
        m_mainActivity.setDetectionStrategy(new QRCodeEnsembleDetectionModeStrategy(m_mainActivity, (QRCodeEnsemble)detectedQR));
    }

    /**
     * Starting a detection of QRCodeQuestion / Reponse
     *
     * @param detectedQR
     */
    private void startQuestionReponseDetection(QRCode detectedQR){
        //Adding the QRCode to the detected ones
        m_detectedQRCodes.addQR(detectedQR);

        //Changing current detection state
        m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);

        //Reading the QR
        m_mainActivity.readQuestion(((QRCodeQuestion)detectedQR).getQuestionText());

        //Launching the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        QRCodeQuestion question;

        if(detectedQR instanceof QRCodeQuestion){
            question = (QRCodeQuestion)detectedQR;
        } else {
            question = null;
        }

        //Changing detection strategy
        m_mainActivity.setDetectionStrategy(new QRCodeQuestionReponseDetectionModelStrategy(m_mainActivity, question));
    }

}
