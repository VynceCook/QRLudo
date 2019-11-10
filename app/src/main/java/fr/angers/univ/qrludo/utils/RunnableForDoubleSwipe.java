package fr.angers.univ.qrludo.utils;

import android.util.Log;

import java.util.ArrayList;

import fr.angers.univ.qrludo.QR.model.QRCodeAtomique;
import fr.angers.univ.qrludo.activities.MainActivity;

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

public class RunnableForDoubleSwipe implements Runnable{

    MainActivity m_mainActivity;
    private int m_currentQRCode;
    private ArrayList<QRCodeAtomique> m_qrCodesToRead;
    boolean posted = false;



    public RunnableForDoubleSwipe(MainActivity m_mainActivity, int m_currentQRCode, ArrayList<QRCodeAtomique> m_qrCodesToRead) {
        this.m_mainActivity = m_mainActivity;
        this.m_currentQRCode = m_currentQRCode;
        this.m_qrCodesToRead = m_qrCodesToRead;
    }


    @Override
    public void run() {
        m_mainActivity.rewindFiveSeconds();
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
                        //m_mainActivity.onSwipeLeft();
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

        posted = false;
        Log.i("Rewind", String.valueOf(posted));
    }
}
