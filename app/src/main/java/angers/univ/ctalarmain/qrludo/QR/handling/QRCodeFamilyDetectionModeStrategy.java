package angers.univ.ctalarmain.qrludo.QR.handling;

import android.util.Log;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

import static angers.univ.ctalarmain.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;

/**
 * Created by Jules Leguy on 29/01/18.
 */
public class QRCodeFamilyDetectionModeStrategy extends QRCodeDetectionModeStrategy {

    //Keeping a reference to the family name so that QRCodes belonging to different families can be ignored
    private String m_familyName;
    private boolean m_isFirstQRDetected;

    public QRCodeFamilyDetectionModeStrategy(MainActivity mainActivity, String familyName, boolean isFirstQRDetected) {
        super(mainActivity);

        m_familyName = familyName;

        m_isFirstQRDetected = isFirstQRDetected;

        //The other QRCodes of the family can be detected again if they have been ignored
        m_detectedQRCodes.clearIgnoredQRComponents();

    }

    /**
     * Should never be called because this strategy is set up only once a first QRCode belonging to a family is detected
     * @param detectedQR
     */
    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("test", "appel à QRCodeFamilyDetectionModeStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {

        Log.v("test", "appel nextdetection famille");

        //Checking that the detected QRCode belongs to the right family
        try {
            if (belongsToFamily(detectedQR) && ((QRCodeAtomique) detectedQR).getFamilyName().equals(m_familyName)){

                Log.v("test", "qr code appartient bonne famille et est donc traité");

                //Adding the QRCode to the list of detected QRCode
                m_detectedQRCodes.addQR(detectedQR);

                //New contentState of the activity
                m_mainActivity.setDetectionProgress(MULTIPLE_QR_DETECTED);

                //Resetting the MultipleDetectionTimer
                m_mainActivity.startMultipleDetectionTimer();

                ToneGeneratorSingleton.getInstance().familyDetectionTone();
            }
            else{
                //Ignoring the QRCodes not belonging to a family or belonging to a different family
                m_detectedQRCodes.addIgnoredQR(detectedQR);

                //Signaling that the QRCode is being ignored
                ToneGeneratorSingleton.getInstance().ignoredQRCodeTone();

                //Resetting the MultipleDetectionTimer
                m_mainActivity.startMultipleDetectionTimer();
            }
        } catch (FamilyException e) {
            e.printStackTrace();
        }


    }

    /**
     * Starts the reading of the detected family
     * If the first QRCode of the family was the first detected QRCode of the current detection, reading the entire collection of detected qr codes
     * by calling familyMultipleReading()
     * If the first detected QRCode of the current detection wasn't part of the family, reading only the next ones by calling classicMultipleReading() because
     * the first has already been read
     */
    @Override
    public void onEndOfMultipleDetectionTimer() {

        m_mainActivity.stopDetection();

        if (m_isFirstQRDetected){
            //If the first QR of the family was the first detected QR of the current detection and therefore no QR has already been printed, adding all the QR
            m_mainActivity.familyMultipleReading();
        }
        else if (m_mainActivity.getDetectionProgress() == MULTIPLE_QR_DETECTED){
            //If the first QR of the family was not the first detected QR of the current detection and therefore one other has already been printed, adding all the QRComponents but the first
            m_mainActivity.classicMultipleReading();
        }

    }

    @Override
    public void onQRFileDownloadComplete() {
        //plays the newly downloaded sound
        m_mainActivity.playCurrentSoundContent();
    }

    @Override
    public void onSwipeTop() {
        if (!m_mainActivity.isApplicationDetecting()){
            //Reading again the current QRContent provided the detection is complete
            m_mainActivity.readCurrentContent();
        }
        else{
            //Signaling that the user cannot swipe top
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeBottom() {
        //Canceling current detection or reading, and starting new detection
        if(!posted) {
            posted = hand.postDelayed(runner, 1000);
        }else{
            m_mainActivity.startNewDetection("Nouvelle détection");
            hand.removeCallbacks(runner);
            posted = false;
        }
    }

    @Override
    public void onSwipeLeft() {

        //The swipe left is possible if one of these two conditions is respected :
        // -The first QRCode of the family was the first detected QR of the current detection so no QRContent has been played during the detection thus the detection must be over
        // -The first QRCode of the family was not the first detected QR of the current detection thus some QRContent has already been played
        if ( (m_isFirstQRDetected && !m_mainActivity.isApplicationDetecting()) || (!m_isFirstQRDetected) ){

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

        //The swipe left is possible if one of these two conditions is respected :
        // -The first QRCode of the family was the first detected QR of the current detection so no QRContent has been played during the detection thus the detection must be over
        // -The first QRCode of the family was not the first detected QR of the current detection thus some QRContent has already been played
        if ( (m_isFirstQRDetected && !m_mainActivity.isApplicationDetecting()) || (!m_isFirstQRDetected) ){

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

}
