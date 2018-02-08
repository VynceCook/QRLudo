package angers.univ.ctalarmain.qrludo.QR.handling;

import android.util.Log;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeEnsemble;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;

/**
 * Created by Jules Leguy on 04/02/18.
 */
public class QRCodeEnsembleDetectionModeStrategy extends QRCodeDetectionModeStrategy {

    QRCodeEnsembleDetectionModeStrategy(MainActivity mainActivity) {
        super(mainActivity);
    }


    /**
     * Should never be called because this strategy is set up only once a first QRCodeEnsemble is detected
     * @param detectedQR
     */
    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("test", "appel à QRCodeEnsembleDetectionModeStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {

        if (detectedQR instanceof QRCodeEnsemble) {

            //Adding the QRCode to the detected ones
            m_detectedQRCodes.addQR(detectedQR);

            //Setting the new detection state
            m_mainActivity.setDetectionProgress(MainActivity.MULTIPLE_QR_DETECTED);

            //Resetting the MultipleDetectionTimer
            m_mainActivity.startMultipleDetectionTimer();

            //Signaling that a new QRCodeEnsemble has been detected
            ToneGeneratorSingleton.getInstance().ensembleDetectionTone();

        }
        else{
            ToneGeneratorSingleton.getInstance().ignoredQRCodeTone();
            m_detectedQRCodes.addIgnoredQR(detectedQR);
        }

    }

    /**
     * Launching ensembleReading() so that the user is told about the state of the downloading of the files
     * Also stopping the detection until all the files have been downloaded or the user cancels
     */
    @Override
    public void onEndOfMultipleDetectionTimer() {
        m_mainActivity.stopDetection();
        m_mainActivity.ensembleReading();
    }

    @Override
    public void onQRFileDownloadComplete() {

        //If all the files have been downloaded, notifying the user
        if (m_mainActivity.areAllQRFilesDownloaded()){
            m_mainActivity.ensembleReadingCompleted();
        }

    }

    @Override
    public void onSwipeTop() {
        //The user cannot swipe top in case of ensemble reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onSwipeBottom() {

        //If all the QRFiles haven't been downloaded, starting a new detection notifying it
        if (!m_mainActivity.areAllQRFilesDownloaded()) {
            m_mainActivity.startNewDetection("Téléchargement annulé");
        }
        else{
            m_mainActivity.startNewDetection("Nouvelle détection");
        }
    }

    @Override
    public void onSwipeLeft()
    {
        //Same behaviour as onSwipeBottom
        onSwipeBottom();
    }

    @Override
    public void onSwipeRight() {
        //The user cannot swipe right in case of ensemble reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

}
