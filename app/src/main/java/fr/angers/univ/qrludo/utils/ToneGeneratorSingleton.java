package fr.angers.univ.qrludo.utils;

import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * Created by Jules Leguy on 29/01/18.
 *
 * Tone Generator as a Singleton so that it can be accessed by any object
 */
public class ToneGeneratorSingleton
{
    private ToneGenerator m_toneGen;

    private ToneGeneratorSingleton()
    {
        m_toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    private static ToneGeneratorSingleton INSTANCE = new ToneGeneratorSingleton();

    public static ToneGeneratorSingleton getInstance()
    {   return INSTANCE;
    }

    public void QRCodeNormallyDetectedTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);
    }



    public void familyDetectionTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_MED_SLS, 50);
    }

    public void ensembleDetectionTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_CALLDROP_LITE, 50);
    }

    public void errorTone(){
        m_toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 15);
    }

    public void ignoredQRCodeTone(){
        m_toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 15);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        m_toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 15);
    }

    public void endOfDetectionTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT);
    }

    public void startingDetectionTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE);
    }

    public void lastQRCodeReadTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_MED_PBX_SLS, 25);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_MED_PBX_SLS, 25);
    }

    public void firstQRCodeReadTone(){
        m_toneGen.startTone(ToneGenerator.TONE_CDMA_MED_PBX_SLS, 25);
    }

}
