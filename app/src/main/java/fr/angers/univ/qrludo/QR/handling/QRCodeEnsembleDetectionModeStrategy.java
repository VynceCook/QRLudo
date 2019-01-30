package fr.angers.univ.qrludo.QR.handling;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeAtomique;
import fr.angers.univ.qrludo.QR.model.QRCodeEnsemble;
import fr.angers.univ.qrludo.QR.model.QRContent;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Created by Jules Leguy on 04/02/18.
 */
public class QRCodeEnsembleDetectionModeStrategy extends QRCodeDetectionModeStrategy {

    private int m_currentQRCode;
    private ArrayList<QRCodeAtomique> m_qrCodesToRead;

    QRCodeEnsembleDetectionModeStrategy(MainActivity mainActivity, QRCodeEnsemble qrcode) {
        super(mainActivity);
        m_currentQRCode = 0;
        m_qrCodesToRead = qrcode.getQRCodes();
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
        m_mainActivity.setCurrentReading(m_qrCodesToRead.get(m_currentQRCode).getQRContent(), -1);

    }

    @Override
    public void onQRFileDownloadComplete() {

        //If all the files have been downloaded, notifying the user
        if (m_mainActivity.areAllQRFilesDownloaded()){
            m_mainActivity.ensembleReadingCompleted();
            m_mainActivity.setCurrentReading(m_qrCodesToRead.get(m_currentQRCode).getQRContent(), -1);
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
            if(!posted) {
                posted = hand.postDelayed(runner, 10000);
            }else{
                m_mainActivity.startNewDetection("Nouvelle détection");
                hand.removeCallbacks(runner);
                posted = false;
            }
        }
    }

    @Override
    public void onSwipeLeft() {
        //Can only swipe left if at least one QR has been printed/detected (equivalent in the default detection mode)
        if (m_mainActivity.getDetectionProgress() != NO_QR_DETECTED) {

            //If the application is still detecting and the user has already reached the last currently available QRContent, cannot swipe left
            if (!(m_mainActivity.isApplicationDetecting() && m_mainActivity.getCurrentPos() == m_mainActivity.getContentSize() - 1)) {

                if (m_mainActivity.getCurrentPos()==m_mainActivity.getContentSize()-1){
                    //Ending the reading if the user had already reached the last QRContent
                    if(m_currentQRCode == m_qrCodesToRead.size() - 1){
                        ToneGeneratorSingleton.getInstance().errorTone();
                    } else{
                        m_currentQRCode++;
                        m_mainActivity.setCurrentReading(m_qrCodesToRead.get(m_currentQRCode).getQRContent(), -1);
                        this.onSwipeLeft();
                    }
                }
                else{
                    //Reading the next QRContent
                    m_mainActivity.incrementCurrentPos();
                    //If the app is waiting to be notified by the current QRFile of the end of its downloading, unregister as listener
                    m_mainActivity.unregisterToQRFile();
                    m_mainActivity.readCurrentContent();

                    if (m_mainActivity.getCurrentPos()==m_mainActivity.getContentSize()-1){
                        //Notifying the user if he has just reached the last QRContent
                        ToneGeneratorSingleton.getInstance().lastQRCodeReadTone();
                    }
                }


            } else {
                ToneGeneratorSingleton.getInstance().errorTone();
            }
        } else {
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
                if(m_currentQRCode == 0){
                    ToneGeneratorSingleton.getInstance().errorTone();
                } else {
                    m_currentQRCode--;
                    List<QRContent> newList = m_qrCodesToRead.get(m_currentQRCode).getQRContent();
                    m_mainActivity.setCurrentReading(newList, newList.size());
                    this.onSwipeRight();
                }
            }
            else{
                if(m_mainActivity.getCurrentPos() != m_qrCodesToRead.get(m_currentQRCode).getQRContent().size()){
                    //If the app is waiting to be notified by the current QRFile of the end of its downloading, unregister as listener
                    m_mainActivity.unregisterToQRFile();
                }

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

}
