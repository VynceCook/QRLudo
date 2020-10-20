package fr.angers.univ.qrludo.QR.handling;


import android.util.Log;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeAtomique;
import fr.angers.univ.qrludo.QR.model.QRCodeEnsemble;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestion;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestionQCM;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestionVocaleOuverte;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestionVocaleQCM;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.FIRST_QR_DETECTED;
import static fr.angers.univ.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;
import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

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
        if (!ensembleBehaviour(detectedQR, true) && !questionReponseBehaviour(detectedQR, true) && !qcmBehaviour(detectedQR, true)
                && !reconnaissanceVocaleBehaviour(detectedQR, true) && !reconnaissanceVocaleQuestionOuverteBehaviour(detectedQR, true)){

            if(detectedQR instanceof QRCodeAtomique) {
                QRCodeAtomique tmpQr = (QRCodeAtomique) detectedQR;
                if (tmpQr.isWebsite())
                    m_mainActivity.setNextQRIsWeb();
            }

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
        if (!ensembleBehaviour(detectedQR, false) && !(questionReponseBehaviour(detectedQR, false)) && !qcmBehaviour(detectedQR, false)
                && !reconnaissanceVocaleBehaviour(detectedQR, false) && !reconnaissanceVocaleQuestionOuverteBehaviour(detectedQR, false)){

            //Building QR and adding it to the detected QRCodes
            m_detectedQRCodes.addQR(detectedQR);

            //New contentState of the activity
            m_mainActivity.setDetectionProgress(MULTIPLE_QR_DETECTED);

            //Resetting the MultipleDetectionTimer
            m_mainActivity.startMultipleDetectionTimer();
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
        m_mainActivity.playCurrentSoundFromFile();
    }

    /*
        If you swipe top once, you rewind the audio from 5 seconds.
        If you swipe top twice, you replay the current detection from the start
    */
    @Override
    public void onSwipeTop() {
        //Launch runnerRewind if you don't swipe another time in the next 1 second
        // runnerRewind is in QRCodeDetectionModeStrategy
        if(!posted) {
            posted = hand.postDelayed(runnerRewind, 1000);
        }
        //Stop runnerRewind if you double swipe and rewind the current audio
        else{
            hand.removeCallbacks(runnerRewind);

            if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
                //Reading again the current QRContent provided at least one QR has been detected.
                m_mainActivity.readCurrentContent();
            }
            else{
                //Signaling that the user cannot swipe top
                ToneGeneratorSingleton.getInstance().errorTone();
            }

            posted = false;
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
     * Starts a QCM detection of ignores the QRCodeQuestionQCM / ReponseQCM if necessary
     * If no action has been executed, returns false
     *
     */
    private boolean qcmBehaviour(QRCode detectedQR, boolean isFirstQRDetected) {
        //Checking if the detected QRCode is a QRCodeEnsemble
        if ((detectedQR instanceof QRCodeQuestionQCM)){

            if (isFirstQRDetected){
                startQuestionReponseDetectionQCM(detectedQR);
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
            Log.v("test", "no QCM behaviour");
            return false;
        }
    }

    /**
     * Starts a question / reponse Vocale detection of ignores the QRCodeQuestion / Reponse if necessary
     * If no action has been executed, returns false
     *
     * @param detectedQR
     * @param isFirstQRDetected
     * @return
     */
    private boolean reconnaissanceVocaleBehaviour(QRCode detectedQR, boolean isFirstQRDetected) {
        //Checking if the detected QRCode is a QRCodeQuestionVocaleQCM
        if ((detectedQR instanceof QRCodeQuestionVocaleQCM)){

            if (isFirstQRDetected){
                startReconnaissanceVocaleQCM(detectedQR);
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
            Log.v("test", "no QCM vocale behaviour");
            return false;
        }
    }

    /**
     * Starts a question ouverte / reponse Vocale detection of ignores the QRCodeQuestion / Reponse if necessary
     * If no action has been executed, returns false
     *
     * @param detectedQR
     * @param isFirstQRDetected
     * @return
     */
    private boolean reconnaissanceVocaleQuestionOuverteBehaviour(QRCode detectedQR, boolean isFirstQRDetected) {
        //Checking if the detected QRCode is a QRCodeQuestionVocaleQuestionOuverte
        if ((detectedQR instanceof QRCodeQuestionVocaleOuverte)){

            if (isFirstQRDetected){
                startReconnaissanceVocaleQuestionOuverte(detectedQR);
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
            Log.v("test", "no Question vocale ouverte behaviour");
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
        m_mainActivity.readPrint(((QRCodeQuestion)detectedQR).getQuestionText());

        //Launching the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        QRCodeQuestion question;

        if(detectedQR instanceof QRCodeQuestion){
            question = (QRCodeQuestion)detectedQR;
        } else {
            question = null;
        }

        //Changing detection strategy
        m_mainActivity.setDetectionStrategy(new QRCodeExerciceDetectionModelStrategy(m_mainActivity, question));
    }

    /**
     * Starting a detection of QRCodeQuestionQCM / ReponseQCM
     *
     * @param detectedQR
     */
    private void startQuestionReponseDetectionQCM(QRCode detectedQR){
        //Adding the QRCode to the detected ones
        m_detectedQRCodes.addQR(detectedQR);

        ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();

        //Changing current detection state
        m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);

        //Reading the QR
        m_mainActivity.readPrint(((QRCodeQuestionQCM)detectedQR).getText());

        //Launching the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        QRCodeQuestionQCM question;

        if(detectedQR instanceof QRCodeQuestionQCM){
            question = (QRCodeQuestionQCM)detectedQR;
        } else {
            question = null;
        }

        //Changing detection strategy
        m_mainActivity.setDetectionStrategy(new QRCodeQCMDetectionModeStrategy(m_mainActivity, question));
    }

    /**
     * Starting a detection of QRCodeQuestionVocaleQCM
     *
     * @param detectedQR
     */
    private void startReconnaissanceVocaleQCM(QRCode detectedQR){
        //Adding the QRCode to the detected ones
        m_detectedQRCodes.addQR(detectedQR);

        ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();

        //Changing current detection state
        m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);

        //Reading the QR
        m_mainActivity.readPrint(((QRCodeQuestionVocaleQCM)detectedQR).getQuestionText());

        //Launching the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        QRCodeQuestionVocaleQCM question;

        if(detectedQR instanceof QRCodeQuestionVocaleQCM){
            question = (QRCodeQuestionVocaleQCM)detectedQR;
        } else {
            question = null;
        }

        //Changing detection strategy
        m_mainActivity.setDetectionStrategy(new QRCodeExerciceVocaleDetectionModeStrategy(m_mainActivity, question));
    }

    /**
     * Starting a detection of {@link QRCodeQuestionVocaleOuverte}
     *
     * @param detectedQR
     */
    private void startReconnaissanceVocaleQuestionOuverte(QRCode detectedQR){
        //Adding the QRCode to the detected ones
        m_detectedQRCodes.addQR(detectedQR);

        ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();

        //Changing current detection state
        m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);

        //Reading the QR
        m_mainActivity.readPrint(((QRCodeQuestionVocaleOuverte)detectedQR).getQuestionText());

        //Launching the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        QRCodeQuestionVocaleOuverte question;

        if(detectedQR instanceof QRCodeQuestionVocaleOuverte){
            question = (QRCodeQuestionVocaleOuverte)detectedQR;
        } else {
            question = null;
        }

        //Changing detection strategy
        m_mainActivity.setDetectionStrategy(new QRCodeExerciceVocaleQuestionOuverteDetectionModeStrategy(m_mainActivity, question));
    }

}
