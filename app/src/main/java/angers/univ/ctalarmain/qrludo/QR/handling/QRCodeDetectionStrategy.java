package angers.univ.ctalarmain.qrludo.QR.handling;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeCollection;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;

/**
 * Created by Jules Leguy on 29/01/18.
 * Uses Strategy design pattern to manage the detection of QRCode depending on the current state of the application
 */
public abstract class QRCodeDetectionStrategy {

    MainActivity m_mainActivity;
    QRCodeCollection m_detectedQRCodes;


    QRCodeDetectionStrategy(MainActivity mainActivity){
        m_detectedQRCodes = mainActivity.getDetectedQRCodes();
        m_mainActivity = mainActivity;
    }

    /**
     * Called when the activity detects the first QRCode of the current detection
     * @param detectedQR
     */
    public abstract void onFirstDetectionWithTimeNotNull(QRCode detectedQR);


    /**
     * Called when the activity detects a QRCode which is not the first of the current detection
     * @param detectedQR
     */
    public abstract void onNextDetectionWithTimeNotNull(QRCode detectedQR);

    
    /**
     * Called by the activity at the end of the MultipleDetectionTimer if new QRCodes have been detected
     */
    public abstract void onEndOfMultipleDetectionWithNewDetections();

    /**
     * Called by the activity at the end of the MultipleDetectionTimer if no new QRCodes has been detected
     */
    public abstract void onEndOfMultipleDetectionWithoutNewDetection();


    /**
     * Used by the subclasses to determine if the given QR Code belongs to a family
     *
     * @param detectedQR
     * @return
     */
    protected boolean belongsToFamily(QRCode detectedQR){
        return (detectedQR instanceof QRCodeAtomique && ((QRCodeAtomique) detectedQR).belongsToFamily());
    }
}
