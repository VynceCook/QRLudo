package angers.univ.ctalarmain.qrludo.QR.handling;


import android.os.Handler;
import android.util.Log;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeEnsemble;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

import static angers.univ.ctalarmain.qrludo.activities.MainActivity.FIRST_QR_DETECTED;
import static angers.univ.ctalarmain.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;
import static angers.univ.ctalarmain.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Created by Jules Leguy on 29/01/18.
 *
 * Default detecting strategy used while no special QRCode has been detected
 *
 * If a QRCode belonging to a family is detected and it is the first of its family, launching QRCodeFamilyDetectionModeStrategy
 * by calling startFamilyDetection(). If the detected QRCode isn't the first of its family, ignoring it.
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
        if (!familyBehaviour(detectedQR, true) && !ensembleBehaviour(detectedQR, true)){

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

        Log.v("test", "DefaultStrategy : onNextDetectionWithTimeNotNull");

        //Applies a family or ensemble related behaviour if necessary or records the detected QR Code
        if (!familyBehaviour(detectedQR, false) && !ensembleBehaviour(detectedQR, false)){

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
        m_mainActivity.playCurrentSoundContent();
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


    /**
     * Starts a family detection or ignores a family QRCode if necessary
     * If no action has been executed, returns false
     * @param detectedQR
     * @param isFirstQRDetected : needs to know if the QR if the first qr detected so that the info can be transmitted to startFamilyDetection
     * @return
     */
    private boolean familyBehaviour(QRCode detectedQR, boolean isFirstQRDetected){

        //checking if the qrcode belongs to a family and if it is the first one
        if (isStartingFamily(detectedQR)){
            Log.v("test", "starting family detection");
            startFamilyDetection(detectedQR, isFirstQRDetected);
            return true;
        }
        //the QRCode belongs to a family but doesn't start one
        else if (belongsToFamily(detectedQR)){
            //signaling the error and ignoring the QRCode
            ToneGeneratorSingleton.getInstance().ignoredQRCodeTone();
            m_detectedQRCodes.addIgnoredQR(detectedQR);
            return true;
        }
        else{
            Log.v("test", "no family behaviour");
            return false;
        }

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
     * Starting a family detection
     *
     * @param detectedQr
     * @param isFirstQRDetected needs to know if the QR is the first detected or if other QR not belonging to the family have been detected before
     *                        so that the detectionState can be set properly
     */
    private void startFamilyDetection(QRCode detectedQr, boolean isFirstQRDetected){
        try {

            //Family detection tone
            ToneGeneratorSingleton.getInstance().familyDetectionTone();

            //Adding the first QRCode of the family to the detected ones
            m_detectedQRCodes.addQR(detectedQr);

            //Changing curent detection state
            if (isFirstQRDetected) {
                m_mainActivity.setDetectionProgress(FIRST_QR_DETECTED);
            }
            else {
                m_mainActivity.setDetectionProgress(MULTIPLE_QR_DETECTED);
            }

            //Launching the MultipleDetectionTimer. At its end, playing the entire detected family
            m_mainActivity.startMultipleDetectionTimer();

            //Changing detection strategy
            m_mainActivity.setDetectionStrategy(new QRCodeFamilyDetectionModeStrategy(m_mainActivity, ((QRCodeAtomique)detectedQr).getFamilyName(), isFirstQRDetected));

        } catch (FamilyException e) {
            //Won't happen if the method is adequately used
            e.printStackTrace();
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
        m_mainActivity.setDetectionStrategy(new QRCodeEnsembleDetectionModeStrategy(m_mainActivity));
    }


    /**
     * Checks if the QRCode is the first of a family
     *
     * @param detectedQR
     * @return
     */
    private boolean isStartingFamily(QRCode detectedQR){

        if (detectedQR instanceof QRCodeAtomique){
            QRCodeAtomique qrAtomique = (QRCodeAtomique) detectedQR;
            try {
                return (qrAtomique.belongsToFamily() && qrAtomique.getFamilyRank()==1);
            } catch (FamilyException e) {
                //won't happen because the test has been made
                return false;
            }
        }
        else{
            return false;
        }
    }


}
