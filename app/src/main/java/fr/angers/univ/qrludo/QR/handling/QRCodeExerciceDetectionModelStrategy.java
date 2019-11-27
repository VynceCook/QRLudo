package fr.angers.univ.qrludo.QR.handling;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeAtomique;
import fr.angers.univ.qrludo.QR.model.QRCodeQuestion;
import fr.angers.univ.qrludo.QR.model.QRCodeReponse;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.NO_QR_DETECTED;

/**
 * Valentine Rahier
 */
public class QRCodeExerciceDetectionModelStrategy extends QRCodeDetectionModeStrategy {

    private QRCodeQuestion m_question;
    private boolean scan_reponse;
    private boolean mode_exploration = false;
    private boolean mode_reponse = false;
    //Représente le tableau avec les id des bonne réponse déjà lu par l'utilisateur
    private ArrayList<String> m_tab_reponse_trouve= new ArrayList<>();

    QRCodeExerciceDetectionModelStrategy(MainActivity mainActivity, QRCodeQuestion question) {
        super(mainActivity);
        m_question = question;
        scan_reponse = false;
        if(m_question!=null) {
            m_mainActivity.modeExploration(m_question.getQuestionText());
            mode_exploration = true;
        }
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("first_detection_qrep", "appel à QRCodeExerciceDetectionModelStrategy.onFirstDetectionWithTimeNotNull() ; ne devrait pas arriver");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {

        //Si le booléen du mode exploration est vrai on éxecute ce mode de detection pour le prochain qrCode qui est détecté
        if(mode_exploration) {
            if (m_question != null) {
                if ((detectedQR instanceof QRCodeAtomique)) {
                    QRCodeAtomique reponse = (QRCodeAtomique) detectedQR;

                    m_mainActivity.readPrint(reponse.getM_reponse());
                    //Le sleep permet de retarder la prochaine detection en mode exploration
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Si le booléen du mode reponse est vrai on éxecute ce mode de détection pour le prochiain qrCode qui est détecté
        if(mode_reponse){
            if (m_question != null) {

                //On limite la lecture à un QrUnique
                if ((detectedQR instanceof QRCodeAtomique)) {
                    QRCodeAtomique reponse = (QRCodeAtomique) detectedQR;

                        //On vérifie si le QrCode lu est une bonne réponse
                        if (m_question.getListe_bonne_rep().contains(reponse.getM_id())) {

                            m_mainActivity.readPrint(reponse.getM_reponse());
                            m_mainActivity.read(m_question.getM_text_bonne_rep());

                            //On vérifie si le QrCode lu n'a pas déjaà été lu par l'utilisateur
                            if(!m_tab_reponse_trouve.contains(reponse.getM_id())) {

                                m_tab_reponse_trouve.add(reponse.getM_id());

                                //Si le nombre d'élément dans le le tableau de réponse trouvé est égale au
                                // nombre de bonne réponse demandé l'exercice est terminé et nous revenons en mode normal
                                if (m_question.getNb_min_reponses() == m_tab_reponse_trouve.size()) {

                                    m_mainActivity.read("Tu as réussi l'exercice");

                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    m_mainActivity.startNewDetection("Nouvelle détection");
                                }
                                //Sinon on indique le nombre de bonne réponse demandé et le nombre de bonne réponse trouvé
                                else {
                                    m_mainActivity.read("Tu as trouvé "
                                            + m_tab_reponse_trouve.size()
                                            + " bonne réponse sur "
                                            + m_question.getNb_min_reponses()
                                            + " bonne réponse");
                                }
                            }
                        //Sinon on indique le méssage de la mauvaise réponse et on indique que c'est une mauvaise réponse
                        } else{
                            m_mainActivity.readPrint(reponse.getM_reponse());
                            m_mainActivity.read(m_question.getM_text_mauvaise_rep());
                        }

                    //On retarde la prochaine détéction fluidifié la détection
                    try {
                        Thread.sleep(1000);
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
        if (m_mainActivity.getDetectionProgress()!=NO_QR_DETECTED){
            if(m_question != null){
                m_mainActivity.readPrint(m_question.getQuestionText());
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

    //On lance le mode détection de bonne réponse ce qui arrète le mode exploration
    @Override
    public void onSwipeLeft() {
        scan_reponse = true;
        m_mainActivity.readPrint("Détection de la réponse");
        mode_reponse = true;
        mode_exploration = false;
    }

    //Relance le mode éxploration
    @Override
    public void onSwipeRight() {
        m_mainActivity.read("Mode exploration");
        mode_reponse = false;
        mode_exploration = true;
    }

    @Override
    public void onDoubleClick() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }
}
