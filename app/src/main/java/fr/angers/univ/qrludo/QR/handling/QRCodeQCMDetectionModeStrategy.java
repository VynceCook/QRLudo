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
    private ArrayList<QRCode> tabOfQRReponse;
    private ContentDelayCounter timer;


    QRCodeQCMDetectionModeStrategy(MainActivity mainActivity, QRCodeQuestionQCM m_question) {
        super(mainActivity);
        this.m_question = m_question;
        scan_reponse = false;
        tabOfQRReponse = new ArrayList<QRCode>();
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeQCMDetectionModeStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        if(scan_reponse){
            //Adding Reponse QCM into tabQRCodeQCM (if not already added)
            if(detectedQR instanceof QRCodeReponseQCM){
                QRCodeReponseQCM detectedQRReponseQCM = (QRCodeReponseQCM) detectedQR;
                addQRReponseToTab(detectedQRReponseQCM);

                //Lancement de timer permettant d'attendre que tous les QR Réponses soient scannés avant de
                //vérifier la réponse
                if (tabOfQRReponse.size() == 1)
                {
                    timer = new ContentDelayCounter();
                    timer.delegate = this;
                    timer.execute();
                }
            }
        }

    }

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
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
            if(m_question != null){
                m_mainActivity.readQuestion(m_question.getText());
            }
        }
        else{
            //Signaling that the user cannot swipe top
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeBottom() {
        //Canceling current detection or reading, and starting new detection, provided the tts is ready
        if (m_mainActivity.isTTSReady()) {
            if(!posted) {
                posted = hand.postDelayed(runner, 1000);
            }else{
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
        //Can only swipe left if at least one QR has been printed/detected (equivalent in the default detection mode)
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){

            //If the application is still detecting and the user has already reached the last currently available QRContent, cannot swipe left
            if (!(m_mainActivity.isApplicationDetecting() && m_mainActivity.getCurrentPos()==m_mainActivity.getContentSize()-1)){
                scan_reponse = true;
                m_mainActivity.readQuestion("Détection de la réponse");
            }
            else{
                ToneGeneratorSingleton.getInstance().errorTone();
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeRight() {
        //The user cannot swipe right in case of QCM reading
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    @Override
    public void onDoubleClick() {

    }

    public void processFinish(Boolean output) {
        if(tabOfQRReponse.size()>=4){
            Log.i("DETECTION MULTIPLE","Erreur : Trop de réponse sont scannées");
            m_mainActivity.readQuestion("Trop de réponse sont scannées");
            tabOfQRReponse.clear();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(tabOfQRReponse.size()==3){
            if(tabOfQRReponse.size()==3){
                boolean reponseBonne = false;

                for(int i=0; i<tabOfQRReponse.size(); ++i){
                    QRCodeReponseQCM tempQuestionQCM = (QRCodeReponseQCM) tabOfQRReponse.get(i);
                    reponseBonne = reponseBonne || tempQuestionQCM.isAnswer();
                }

                if(reponseBonne){
                    m_mainActivity.readQuestion("Mauvaise Reponse");
                }
                else{
                    m_mainActivity.readQuestion("Bonne Reponse");
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                m_mainActivity.startNewDetection("Nouvelle détection");
                scan_reponse = false;
            }
        }
    }
}
