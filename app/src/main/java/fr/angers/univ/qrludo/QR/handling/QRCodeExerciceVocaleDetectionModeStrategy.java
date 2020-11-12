package fr.angers.univ.qrludo.QR.handling;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeAtomique;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestion;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestionVocaleQCM;
import fr.angers.univ.qrludo.action.CaptureSpeech;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.MULTIPLE_PERMISSIONS;
import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;
import static fr.angers.univ.qrludo.activities.MainActivity.ONE_PERMISSION;
import static fr.angers.univ.qrludo.activities.MainActivity.SPEECH_REQUEST;

/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeExerciceVocaleDetectionModeStrategy extends QRCodeDetectionModeStrategy{
    private QRCodeQuestionVocaleQCM m_question;
    private String m_reponse ; // Réponse renvoyé par la reconnaisance vocale
    //Représente le tableau avec les id des bonne réponse déjà lu par l'utilisateur
    //private ArrayList<String> m_tab_reponse_trouve= new ArrayList<>();
    boolean firstRead;

    public QRCodeExerciceVocaleDetectionModeStrategy(MainActivity mainActivity, QRCodeQuestionVocaleQCM question){
        super(mainActivity);
        m_question = question;
        firstRead = true;
        m_reponse ="";

        // On énonce les réponses
        m_mainActivity.read("Les réponses possibles sont :");
        for(Object reponse :m_question.getListe_rep()){
            ArrayList rep = new ArrayList();
            rep = (ArrayList) reponse ;
            Log.i("DebugDPY", rep.toString() );
            m_mainActivity.read(rep.get(0).toString());
            m_mainActivity.read(rep.get(2).toString());

        }
        // Message pour dire qu'elle est la façon de répondre selon lettreRepondeVocale
        m_mainActivity.read("Pour donner la bonne réponse prononcez :");
        if(m_question.getLettreReponseVocale()){
            // On doit répondre par l'identifiant
            for(Object reponse :m_question.getListe_rep()){
                ArrayList rep = new ArrayList();
                rep = (ArrayList) reponse ;
                Log.i("DebugDPY", rep.toString() );
                m_mainActivity.read(rep.get(0).toString());
            }
        }else {
            // On doit répondre par l'énoncé
            for(Object reponse :m_question.getListe_rep()){
                ArrayList rep = new ArrayList();
                rep = (ArrayList) reponse ;
                Log.i("DebugDPY", rep.toString() );
                m_mainActivity.read(rep.get(2).toString());
            }
        }

        for( String s : m_question.getListe_bonne_reponse()){
            Log.i("DebugDPY",s);
        }

    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeExerciceVocaleDetectionModeStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("next_detection_qrep", "appel à QRCodeExerciceVocaleDetectionModeStrategy.onNextDetectionWithTimeNotNull ; ne devrait pas arriver");
    }

    @Override
    public void onEndOfMultipleDetectionTimer() {
    }

    @Override
    public void onQRFileDownloadComplete() {
    }

    @Override
    public void onSwipeTop() {
        repeteQuestion();
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
    public void onSwipeRight() {ToneGeneratorSingleton.getInstance().errorTone(); }

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
            if(m_question.getListe_bonne_reponse().contains(m_reponse.toLowerCase())){
                // On termine la question et on retourne au mode normal
                m_mainActivity.read(m_question.getM_text_bonne_rep());
                m_mainActivity.read("Tu as réussi l'exercice");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                m_mainActivity.startNewDetection("Nouvelle détection");
            }else{//On indique que c'est une mauvaise réponse
                Log.i("Debug : ", m_question.getM_text_mauvaise_rep());
                m_mainActivity.read(m_question.getM_text_mauvaise_rep());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                repeteQuestion();

            }

        }
    }

    public void repeteQuestion(){
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
            if(m_question != null){
                m_mainActivity.makeSilence();
                // On répète la question
                m_mainActivity.readPrint(m_question.getQuestionText());
                m_mainActivity.read("Les réponses possibles sont :");
                //On répète les réponses possibles
                Log.i("DebugDPY","On arrive au reponse" );
                for(Object reponse :m_question.getListe_rep()){
                    ArrayList rep = new ArrayList();
                    rep = (ArrayList) reponse ;
                    Log.i("DebugDPY", rep.toString() );
                    m_mainActivity.read(rep.get(0).toString());
                    m_mainActivity.read(rep.get(2).toString());

                }

                // Message pour dire qu'elle est la façon de répondre selon lettreRepondeVocale
                m_mainActivity.read("Pour donner la bonne réponse prononcez :");
                if(m_question.getLettreReponseVocale()){
                    // On doit répondre par l'identifiant
                    for(Object reponse :m_question.getListe_rep()){
                        ArrayList rep = new ArrayList();
                        rep = (ArrayList) reponse ;
                        Log.i("DebugDPY", rep.toString() );
                        m_mainActivity.read(rep.get(0).toString());
                    }
                }else {
                    // On doit répondre par l'énoncé
                    for(Object reponse :m_question.getListe_rep()){
                        ArrayList rep = new ArrayList();
                        rep = (ArrayList) reponse ;
                        Log.i("DebugDPY", rep.toString() );
                        m_mainActivity.read(rep.get(2).toString());
                    }
                }
            }
        }
        else{
            //Signaling that the user cannot swipe top
            ToneGeneratorSingleton.getInstance().errorTone();
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
                m_mainActivity.startActivityForResult(intent, SPEECH_REQUEST);
                // On notifie l'utilisateur du swipe left
                ToneGeneratorSingleton.getInstance().lastQRCodeReadTone();
            } catch (ActivityNotFoundException a){
                Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_LONG);
            }
        }
    }
}
