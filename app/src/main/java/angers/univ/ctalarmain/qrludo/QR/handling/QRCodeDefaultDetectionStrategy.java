package angers.univ.ctalarmain.qrludo.QR.handling;


import android.util.Log;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

import static angers.univ.ctalarmain.qrludo.activities.MainActivity.FIRST_QR_DETECTED_STATE;
import static angers.univ.ctalarmain.qrludo.activities.MainActivity.MULTIPLE_QR_DETECTED;

/**
 * Created by Jules Leguy on 29/01/18.
 * Default detecting strategy used while no QRCode belonging to a family is detected
 */
public class QRCodeDefaultDetectionStrategy extends QRCodeDetectionStrategy {


    public QRCodeDefaultDetectionStrategy(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {

        //Applies a family related behaviour if necessary or launches the reading of the detected QR Code
        if (!familyBehaviour(detectedQR, true)){
            //adding it to the detected QRCodes
            m_detectedQRCodes.addQR(detectedQR);

            //Changing curent detection state
            m_mainActivity.setDetectionState(FIRST_QR_DETECTED_STATE);

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

        //Applies a family related behaviour if necessary or records the given detected QR Code
        if (!familyBehaviour(detectedQR, false)){

            //Building QR and adding it to the detected QRCodes
            m_detectedQRCodes.addQR(detectedQR);

            //New contentState of the activity
            m_mainActivity.setDetectionState(MULTIPLE_QR_DETECTED);

            //Resetting the MultipleDetectionTimer
            m_mainActivity.startMultipleDetectionTimer();

            ToneGeneratorSingleton.getInstance().QRCodeNormallyDetectedTone();
        }

    }

    @Override
    public void onEndOfMultipleDetection() {
        m_mainActivity.classicMultipleReading();
    }

    /**
     * Starts a family detection or ignore a family QRCode if necessary
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
            ToneGeneratorSingleton.getInstance().familyRelatedErrorTone();
            m_detectedQRCodes.addIgnoredQR(detectedQR);
            return true;
        }
        else{
            Log.v("test", "no family behaviour");
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

        ToneGeneratorSingleton.getInstance().startingFamilyDetectionTone();
        try {

            //Family detection tone
            ToneGeneratorSingleton.getInstance().startingFamilyDetectionTone();

            //Adding the first QRCode of the family to the detected ones
            m_detectedQRCodes.addQR(detectedQr);

            //Changing curent detection state
            if (isFirstQRDetected) {
                m_mainActivity.setDetectionState(FIRST_QR_DETECTED_STATE);
            }
            else {
                m_mainActivity.setDetectionState(MULTIPLE_QR_DETECTED);
            }

            //Launching the MultipleDetectionTimer. At its end, playing the entire detected family
            m_mainActivity.startMultipleDetectionTimer();

            //Changing detection strategy
            m_mainActivity.setDetectionStrategy(new QRCodeFamilyDetectionStrategy(m_mainActivity, ((QRCodeAtomique)detectedQr).getFamilyName(), isFirstQRDetected));

        } catch (FamilyException e) {
            //Won't happen if the method is adequately used
            e.printStackTrace();
        }

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
