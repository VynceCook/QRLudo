package fr.angers.univ.qrludo.QR.handling;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
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

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeExerciceVocaleDetectionModeStrategy extends QRCodeDetectionModeStrategy{
    private QRCodeQuestionVocaleQCM m_question;
    private String m_reponse ; // Réponse renvoyé par la reconnaisance vocale
    //Représente le tableau avec les id des bonne réponse déjà lu par l'utilisateur
    //private ArrayList<String> m_tab_reponse_trouve= new ArrayList<>();
    boolean firstRead;

    QRCodeExerciceVocaleDetectionModeStrategy(MainActivity mainActivity, QRCodeQuestionVocaleQCM question){
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
        Log.i("DebugDPY",this.m_question.getQuestionText() );
        // Répète la question si QRCodeQuestionVocaleQCM de nouveau détecté
        if(m_question != null){
            if((detectedQR instanceof QRCodeQuestionVocaleQCM)) {
                QRCodeQuestionVocaleQCM question = (QRCodeQuestionVocaleQCM) detectedQR;
                Log.i("DebugDPY",question.getQuestionText() );
                Log.i("DebugDPY",this.m_question.getQuestionText() );
                if(question.getQuestionText().equals(this.m_question.getQuestionText())) {

                    //Les QR Codes sont toujours lu dès la détection lorsque l'on n'est pas dans une stratégié
                    //La première fois que l'on arrive dans la stratégie, on ne doit donc pas relire la question
                    if (firstRead) {
                        firstRead = false;
                        Log.i("DebugDPY", "Firstread a faux");
                    }else {
                        m_mainActivity.readPrint(question.getQuestionText());
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeLeft() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onSwipeRight() {
        // On crée l'inent de la reconnaissance vocale
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Dites votre réponse...");
        try{
            // On lance la reconnaissance vocale en envoyant l'intent au Main
            m_mainActivity.startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a){
            Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_SHORT);
        }

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
                //On indique que c'est une mauvaise réponse
            }else{
                m_mainActivity.read(m_question.getM_text_mauvaise_rep());
                repeteQuestion();

            }

        }
    }

    public void repeteQuestion(){

        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
            if(m_question != null){
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
            }
        }
        else{
            //Signaling that the user cannot swipe top
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }
}
