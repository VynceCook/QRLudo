package fr.angers.univ.qrludo.QR.handling;

import android.util.Log;

import java.util.ArrayList;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestionQCM;
import fr.angers.univ.qrludo.QR.model.QRCodeReponseQCM;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ContentDelayCounter;
import fr.angers.univ.qrludo.utils.QDCResponse;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

public class QRCodeQCMDetectionModeStrategy extends QRCodeDetectionModeStrategy implements QDCResponse {
    private QRCodeQuestionQCM m_question;
    private boolean scan_reponse;
    private boolean timer_is_running;
    private ArrayList<QRCode> tabOfQRReponse;
    private ContentDelayCounter timer;


    QRCodeQCMDetectionModeStrategy(MainActivity mainActivity, QRCodeQuestionQCM m_question) {
        super(mainActivity);
        this.m_question = m_question;
        scan_reponse = false;
        timer_is_running = false;
        tabOfQRReponse = new ArrayList<QRCode>();
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeQCMDetectionModeStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        //If strategy is on detection mode
        if(scan_reponse){
            if(detectedQR instanceof QRCodeReponseQCM){
                QRCodeReponseQCM detectedQRReponseQCM = (QRCodeReponseQCM) detectedQR;
                addQRReponseToTab(detectedQRReponseQCM);

                //Lancement de timer permettant d'attendre que tous les QR Réponses soient scannés avant de
                //vérifier la réponse
                if (tabOfQRReponse.size() == 1 && !timer_is_running)
                {
                    timer = new ContentDelayCounter();
                    timer.delegate = this;
                    timer.execute(3);
                    timer_is_running = true;
                }
            }
        }

    }

    //Adding Reponse QCM into tabQRCodeQCM (if not already added)
    public void addQRReponseToTab(QRCodeReponseQCM QRReponseQCM){
        boolean isAlreadyInTab=false;
        for(int j = 0; j<tabOfQRReponse.size();++j){
            if(tabOfQRReponse.get(j)instanceof QRCodeReponseQCM){
                QRCodeReponseQCM tempQuestionQCM = (QRCodeReponseQCM) tabOfQRReponse.get(j);
                if(QRReponseQCM.getId().equals(tempQuestionQCM.getId())) {
                    isAlreadyInTab = true;
                }
            }
        }
        if(!isAlreadyInTab){
            tabOfQRReponse.add(QRReponseQCM);
            Log.i("DETECTION MULTIPLE",QRReponseQCM.getId());
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
        //Launch runnerRewind if you don't swipe another time in the next 1 second
        // runnerRewind is in QRCodeDetectionModeStrategy
        if(!posted) {
            posted = hand.postDelayed(runnerRewind, 1000);
        }
        else{
            //Stop runnerRewind if you double swipe and rewind the current audio
            hand.removeCallbacks(runnerRewind);

            if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
                //Reading again the current QRContent provided at least one QR has been detected.
                m_mainActivity.makeSilence();
                m_mainActivity.read(m_question.getText());
                //m_mainActivity.readQuestion(m_question.getText());
            }
            else{
                //Signaling that the user cannot swipe top
                ToneGeneratorSingleton.getInstance().errorTone();
            }

            posted = false;
        }
    }

    @Override
    public void onSwipeBottom() {
        //Canceling current detection or reading, and starting new detection, provided the tts is ready
        if (m_mainActivity.isTTSReady()) {
            if(!posted) {
                posted = hand.postDelayed(runner, 1000);
            }else{
                scan_reponse = false;
                m_mainActivity.startNewDetection("Nouvelle détection");
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
        if(scan_reponse){
            ToneGeneratorSingleton.getInstance().errorTone();
        }
        else{
            scan_reponse = true;
            m_mainActivity.makeSilence();

            m_mainActivity.readPrint("Détection de la réponse");
        }
    }

    @Override
    public void onSwipeRight() {
        //The user cannot swipe right in case of QCM reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onDoubleClick() {
        m_mainActivity.pauseCurrentReading();
    }

    //This method start at the end of the timer on detection mode
    public void processFinish(Boolean output) {
        timer_is_running = false;

        //If the user scanned more than 3 answers, he can retry
        if(tabOfQRReponse.size()>=m_question.getNombreReponses()){
            Log.i("DETECTION MULTIPLE","Erreur : Trop de réponses sont scannées");

            m_mainActivity.readPrint("Trop de réponses sont scannées");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tabOfQRReponse.clear();
        }
        //If the user scanned 3 answers, check if the answer is right or wrong
        if(tabOfQRReponse.size()==m_question.getNombreReponses()-1){
                boolean reponseBonneIsDetected = false;

                for(int i=0; i<tabOfQRReponse.size(); ++i){
                    QRCodeReponseQCM tempQuestionQCM = (QRCodeReponseQCM) tabOfQRReponse.get(i);
                    reponseBonneIsDetected = reponseBonneIsDetected || tempQuestionQCM.isAnswer();
                }

                //Good Answer is detected
                if(reponseBonneIsDetected){
                    m_mainActivity.readPrint("Mauvaise Réponse");
                }
                //Good Answer is not detected
                else{
                    m_mainActivity.readPrint("Bonne Réponse");
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Launch new detection
                scan_reponse = false;
                m_mainActivity.startNewDetection("Nouvelle détection");
        }
        //If the user scanned less than 3 answers, he can retry
        else{
            Log.i("DETECTION MULTIPLE","Reset");
            tabOfQRReponse.clear();
        }
    }
}
