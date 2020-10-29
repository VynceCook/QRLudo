package fr.angers.univ.qrludo.QR.handling;

import android.content.ContentProvider;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeScenarioLoader;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

public class QRCodeScenarioDetectionModeStrategy extends QRCodeDetectionModeStrategy {
    private QRCodeScenarioLoader m_question;

    public QRCodeScenarioDetectionModeStrategy(MainActivity mainActivity, QRCodeScenarioLoader question) {
        super(mainActivity);
        m_question = question;

        m_question.saveDocumentAsXMLFile(mainActivity);




    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {

    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

    }

    @Override
    public void onQRFileDownloadComplete() {

    }

    @Override
    public void onSwipeTop() {
        Toast.makeText(m_mainActivity, "Saved to " + m_mainActivity.getApplicationContext().getFilesDir()+"/"+m_question.getFILENAME(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSwipeBottom() {
        //Canceling current detection or reading, and starting new detection, provided the tts is ready
        if (m_mainActivity.isTTSReady()) {
            if(!posted) {
                posted = hand.postDelayed(runner, 1000);
            }else{
                m_mainActivity.startNewDetection("Question annulée, réactivation du mode normal. Nouvelle détection");
                hand.removeCallbacks(runner);
                posted = false;
                supprimeFichierStockageInterne();
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeLeft() {
        // Lire le fichier scenario.xml
        FileInputStream fis = null;
        try {
            fis = m_mainActivity.openFileInput(m_question.getFILENAME());
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }

            Toast.makeText(m_mainActivity, sb.toString(), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis!= null){
                try {
                    fis.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSwipeRight() {

    }

    @Override
    public void onDoubleClick() {

    }

    private void supprimeFichierStockageInterne(){
        Log.i("Debug_scenario","Suppresion scenario");
        Log.i("Debug_scenario","Nombre de fichier dans storage interne "+ m_mainActivity.getApplicationContext().getFilesDir().listFiles().length);
        // delete le fichier scenario .xml
        m_mainActivity.getApplicationContext().deleteFile(m_question.getFILENAME());
        // On regarde si bien supprimé
        Log.i("Debug_scenario","Nombre de fichier dans storage interne "+ m_mainActivity.getApplicationContext().getFilesDir().listFiles().length);
        for(File f : m_mainActivity.getApplicationContext().getFilesDir().listFiles()){
            Log.i("Debug_scenario",f.getName());
        }
    }
}
