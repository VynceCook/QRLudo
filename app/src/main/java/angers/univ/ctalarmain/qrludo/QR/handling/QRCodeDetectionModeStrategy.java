package angers.univ.ctalarmain.qrludo.QR.handling;

import android.os.Handler;
import android.os.Looper;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeCollection;
import angers.univ.ctalarmain.qrludo.activities.MainActivity;

/**
 * Created by Jules Leguy on 29/01/18.
 * Uses Strategy design pattern to manage the detection of QRCode depending on the current state of the application
 */
public abstract class QRCodeDetectionModeStrategy {

    MainActivity m_mainActivity;
    QRCodeCollection m_detectedQRCodes;


    Handler hand = new Handler(Looper.getMainLooper());

    boolean posted = false;

    Runnable runner = new Runnable() {
        @Override
        public void run() {
            m_mainActivity.makeSilence();
            posted = false;
        }
    };

    QRCodeDetectionModeStrategy(MainActivity mainActivity){
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
     * Called by the activity at the end of the MultipleDetectionTimer
     */
    public abstract void onEndOfMultipleDetectionTimer();

    /**
     * Handles the end of the downloading of a QRFile the application is listening to
     */
    public abstract void onQRFileDownloadComplete();

    /**
     * Called when the user swipes towards the top of the screen
     */
    public abstract void onSwipeTop();

    /**
     * Called when the user swipes towards the bottom of the screen
     */
    public abstract void onSwipeBottom();

    /**
     * Called when the user swipes towards the left of the screen
     */
    public abstract void onSwipeLeft();

    /**
     * Called when the user swipes towards the right of the screen
     */
    public abstract void onSwipeRight();

    /**
     * Called when the user double tap on the screen
     */
    public abstract void onDoubleClick();




}
