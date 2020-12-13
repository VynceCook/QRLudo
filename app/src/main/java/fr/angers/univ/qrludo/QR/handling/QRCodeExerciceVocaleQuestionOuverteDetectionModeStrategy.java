package fr.angers.univ.qrludo.QR.handling;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestionVocaleOuverte;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;
import static fr.angers.univ.qrludo.activities.MainActivity.ONE_PERMISSION;
import static fr.angers.univ.qrludo.activities.MainActivity.SPEECH_REQUEST_2;

public class QRCodeExerciceVocaleQuestionOuverteDetectionModeStrategy extends QRCodeDetectionModeStrategy{
    private QRCodeQuestionVocaleOuverte m_question;
    private String m_reponse;// Réponse renvoyé par la reconnaisance vocale


    public QRCodeExerciceVocaleQuestionOuverteDetectionModeStrategy(MainActivity mainActivity, QRCodeQuestionVocaleOuverte question) {
        super(mainActivity);
        m_question = question;
        m_reponse = "";

        // On dit à l'utilisateur qu'il peux répondre
        m_mainActivity.read("A vous de répondre");
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeExerciceVocaleQuestionOuverteDetectionModeStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("next_detection_qrep", "appel à QRCodeExerciceVocaleQuestionOuverteDetectionModeStrategy.onNextDetectionWithTimeNotNull ; ne devrait pas arriver");
    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

    }

    @Override
    public void onQRFileDownloadComplete() {

    }

    @Override
    public void onSwipeTop() {
        // On répète la question
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
            if(m_question != null){
                m_mainActivity.makeSilence();
                m_mainActivity.read(m_question.getQuestionText());
                m_mainActivity.read("A vous de répondre");
            }
        }
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
            }
        }
        else{ToneGeneratorSingleton.getInstance().errorTone();}
    }

    @Override
    public void onSwipeLeft() {
        lancementReconnaissanceVocale();
    }

    @Override
    public void onSwipeRight() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onDoubleClick() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    public void setM_reponse(String reponse){
        m_reponse = reponse;
    }

    /*
     * Fonction appelée à la fin de la reconnaissance vocale dans mainactivity.onActivityResult
     * Verifie selon le mode de réponse si elle est bonne ou non
     */
    public void verifReponse(){
        if(m_question != null){
            // On verifie que la réponse donnée est bonne
            if(m_question.getBonneReponse().equals(m_reponse.toLowerCase())){
                // On termine la question et on retourne au mode normal
                m_mainActivity.read(m_question.getText_bonne_rep());
                m_mainActivity.read("Tu as réussi l'exercice");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                m_mainActivity.startNewDetection("Nouvelle détection");
                //On indique que c'est une mauvaise réponse
            }else{
                m_mainActivity.read(m_question.getM_text_mauvaise_rep());
                m_mainActivity.read(m_question.getQuestionText());
                m_mainActivity.read("A vous de répondre");
            }
        }
    }

    public void lancementReconnaissanceVocale(){
        //On regarde si on la permission d'utiliser le micro
        if(ActivityCompat.checkSelfPermission(m_mainActivity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ToneGeneratorSingleton.getInstance().errorTone();
            // Si non on la demande
            ActivityCompat.requestPermissions(m_mainActivity, new String[]{Manifest.permission.RECORD_AUDIO},ONE_PERMISSION);
            ToneGeneratorSingleton.getInstance().errorTone();
        }else{
            // On crée l'intent de la reconnaissance vocale
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Dites votre réponse...");
            try{
                // On lance la reconnaissance vocale en envoyant l'intent au Main avec le code de SPEECH_REQUEST_2 dans le mainActivity
                m_mainActivity.startActivityForResult(intent, SPEECH_REQUEST_2);
                // On notifie l'utilisateur du swipe left
                ToneGeneratorSingleton.getInstance().lastQRCodeReadTone();
            } catch (ActivityNotFoundException a){
                Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_LONG);
            }
        }
    }
}
