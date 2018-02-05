package angers.univ.ctalarmain.qrludo.QR.handling;

import android.util.Log;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

/**
 * Created by etudiant on 04/02/18.
 */

public class QRCodeEnsembleDetectionStrategy extends QRCodeDetectionStrategy{

    QRCodeEnsembleDetectionStrategy(MainActivity mainActivity) {
        super(mainActivity);
    }


    /**
     * Should never be called because this strategy is set up only once a first QRCodeEnsemble is detected
     * @param detectedQR
     */
    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("test", "appel à QRCodeEnsembleDetectionStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {

        //Adding the QRCode to the detected ones
        m_detectedQRCodes.addQR(detectedQR);

        //Setting the new detection state
        m_mainActivity.setDetectionState(MainActivity.MULTIPLE_QR_DETECTED);

        //Resetting the MultipleDetectionTimer
        m_mainActivity.startMultipleDetectionTimer();

        //Signaling that a new QRCodeEnsemble has been detected
        ToneGeneratorSingleton.getInstance().ensembleDetectionTone();

    }

    /**
     * Launching ensembleReading() so that the user is told about the state of the downloading of the files
     */
    @Override
    public void onEndOfMultipleDetectionWithNewDetections() {
        m_mainActivity.ensembleReading();
    }

    /**
     * Launching ensembleReading() so that the user is told about the state of the downloading of the files
     * Also stopping the detection until all the files have been downloaded or the user cancels
     */
    @Override
    public void onEndOfMultipleDetectionWithoutNewDetection() {
        m_mainActivity.ensembleReading();
        m_mainActivity.stopDetection();
    }
}
