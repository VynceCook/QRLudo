package fr.angers.univ.qrludo.QR.handling;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeReponseSeriousGame;
import fr.angers.univ.qrludo.QR.model.QRCodeSeriousGame;
import fr.angers.univ.qrludo.action.Action;
import fr.angers.univ.qrludo.action.AddNode;
import fr.angers.univ.qrludo.action.CaptureQR;
import fr.angers.univ.qrludo.action.CaptureSpeech;
import fr.angers.univ.qrludo.action.ClearAtoms;
import fr.angers.univ.qrludo.action.ClearNodes;
import fr.angers.univ.qrludo.action.RemoveNode;
import fr.angers.univ.qrludo.action.TTSReading;
import fr.angers.univ.qrludo.action.VerificationConditionFinScenario;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.atom.QRAtom;
import fr.angers.univ.qrludo.atom.SpeechAtom;
import fr.angers.univ.qrludo.scenario.Node;
import fr.angers.univ.qrludo.scenario.ScenarioLoader;
import fr.angers.univ.qrludo.utils.CompressionString;
import fr.angers.univ.qrludo.utils.FileDownloader;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

import static fr.angers.univ.qrludo.activities.MainActivity.SPEECH_REQUEST_3;

public class QRCodeSeriousGameStrategy extends QRCodeDetectionModeStrategy implements FileDownloader.FileDownloaderObserverInterface {
    static private int NOMBRE_MAX_TENTATIVES=3;

    private ScenarioLoader scenario;
    private ArrayList<Node> AllNodes;
    private QRCodeSeriousGame code;
    private MainActivity mainActivity;
    private TextToSpeech text;
    private boolean scan_reponse = false;
    private boolean mode_reponse = false;
    private boolean firstDetection = true;
    private String type_reponse;
    private Node current_node;
    private String reponseSpeech = "vide";
    private QRCodeReponseSeriousGame reponseQR;
    // Tableau de boolean pour savoir si une énigme est résolue
    private ArrayList<Boolean> enigmeResolu;
    private int enigmeResolues = 0;
    // Tableau de boolean pour savoir le nombre de tentatives par énigmes
    private ArrayList<Integer> nombreDeTentatives;

    public QRCodeSeriousGameStrategy(MainActivity mainActivity, QRCodeSeriousGame code){
        super(mainActivity);
        this.code = code;
        this.mainActivity = mainActivity;

        // On sauvegarde le document XML dans le storage interne de l'appareil
        code.saveDocumentAsXMLFile(mainActivity);

        enigmeResolu = new ArrayList<>();
        nombreDeTentatives = new ArrayList<>();
        for(int i = 0; i<(int)code.getDestinations().size(); i++){
            enigmeResolu.add(false);
            nombreDeTentatives.add(0);
        }

        this.scenario = new ScenarioLoader(mainActivity,code.getFILENAME());
        AllNodes = new ArrayList<Node>();
        try {
            this.AllNodes = scenario.getNodes();
        } catch (IOException e){
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        Log.v("Strategy", "Initialisation Serious Game");
        if(code!=null){
            Log.v("Lecture", "Introduction");
            readNode(1);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readNode(2);
        }
    }

    // Fonction qui lit un noeud et ses actions
    public void readNode(int nodeID){
        if(nodeID == 2)
            type_reponse = "speech";
        Log.v("fonction", "readNode "+nodeID);
        current_node = getNode(nodeID);
        if (current_node != null) {
            List<Action> actions = current_node.getActions();
            if (actions.size() > 0) {
                doActions(actions);
            }
        }
    }

    // Fonction qui effectue les actions d'un noeud
    public void doActions(List<Action> actions){
        Log.v("fonction", "doActions");

        for(Action a : actions){
            if(a instanceof TTSReading){
                Log.v("action", "TTSReading");
                readTTSReader((TTSReading) a);
            }
            else if(a instanceof RemoveNode) {
                Log.v("action", "removeNode");
            }
            else if(a instanceof AddNode) {
                Log.v("action", "addNode");
            }
            else if(a instanceof ClearNodes){
                Log.v("action", "ClearNode");
            }
            else if(a instanceof VerificationConditionFinScenario){
                if(code.getDestinations().size() == 0){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    readNode(105);
                    // On quitte le mode Scénario
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    m_mainActivity.startNewDetection("Nouvelle détection");
                    supprimeFichierStockageInterne();
                    break;
                }
            }
            else if(a instanceof ClearAtoms){
                Log.v("action", "ClearAtoms");
            }
            else if(a instanceof CaptureSpeech){
                Log.v("action", "CaptureSpeech");
                if(current_node.ID > 2 && current_node.ID != 105) {
                    mainActivity.read("Ceci est une énigme à reconnaissance vocale");
                    mode_reponse = true;
                    type_reponse = "speech";
                }
            }
            else if(a instanceof CaptureQR){
                Log.v("action", "CaptureQR");
                if(current_node.ID > 2 && current_node.ID != 105) {
                    mainActivity.startDetection();
                    mainActivity.read("Ceci est une énigme à détection de QR code");
                    mode_reponse = true;
                    type_reponse = "qrcode";
                }
            }
        }
    }

    // Fonction qui sert à gérer la résolution des énigmes
    public void enigme(Node bonne_reponse, Node mauvaise_reponse){
        Log.i("Debug_scenario",Boolean.toString(bonne_reponse.getConditions().get(0) instanceof SpeechAtom));
        Log.i("Debug_scenario",Boolean.toString(bonne_reponse.getConditions().get(0) instanceof QRAtom));
        //ENIGME DE TYPE RECO VOCALE
        if(bonne_reponse.getConditions().get(0) instanceof SpeechAtom) {
            if (!reponseSpeech.equals("vide")) {
                // Si c'est la bonne réponse qui est donnée
                if (bonne_reponse.getConditions().get(0).getContent().toLowerCase().equals(reponseSpeech.toLowerCase())) {
                    goodAnswer(bonne_reponse);
                }
                // Si c'est la mauvaise réponse qui est donnée
                else {
                    wrongAnswer(mauvaise_reponse);
                }
            }
        }
        //ENIGME DE TYPE DETETION DE QR CODE
        else if(bonne_reponse.getConditions().get(0) instanceof QRAtom){
            if(reponseQR != null) {
                // Si c'est la bonne réponse qui est donnée
                if (reponseQR.isGoodAnswer()) {
                    goodAnswer(bonne_reponse);
                }
                // Si c'est la mauvaise réponse qui est donnée
                else {
                    wrongAnswer(mauvaise_reponse);
                }
            }
        }
        readNode(2);
    }


    // Action à faire lorsque l'utilisateur mobile à trouvé la bonne réponse
    public void goodAnswer(Node bonne_reponse){
        mainActivity.read("Bonne réponse");
        deleteEnigmeFromList(getCurrentEnigmePosition());
        readNode(bonne_reponse.ID);
    }

    //Action à faire lorsque l'utilisateur mobile à donné une mauvaise réponse
    public void wrongAnswer(Node mauvaise_reponse){
        mainActivity.read("Mauvaise réponse");
        int position = getCurrentEnigmePosition();
        nombreDeTentatives.set(position,nombreDeTentatives.get(position)+1);

        //Nombre de tentatives depassé
        if(nombreDeTentatives.get(position)>NOMBRE_MAX_TENTATIVES-1){
            mainActivity.read("Nombre de tentatives depassées, retrait de l'énigme");
            deleteEnigmeFromList(position);
        }
        //Nombre de tentatives non depassé
        else
            mainActivity.read("Nombre de tentatives restantes : "+ (NOMBRE_MAX_TENTATIVES-nombreDeTentatives.get(position))+"\n");

        readNode(mauvaise_reponse.ID);
    }

    // Cette fonction retourne la position de l'énigme courante dans la liste d'énigme
    public int getCurrentEnigmePosition(){
        for(int i=0; i <(int) code.getDestinations().size(); i++){
            int id = code.idEnigme(code.getDestinations().get(i).toLowerCase());
            if(current_node.ID == (100+id)){
                return i;
            }
        }
        return 0;
    }

    // Fonction permettant de supprimer une énigme de la liste d'énigmes
    public void deleteEnigmeFromList(int i){
        String tts_text = "Choisis une destination ! Parmi, ";
        code.getDestinations().remove(i);
        ++enigmeResolues;
        for(int j = 0; j < code.getDestinations().size(); ++j){
            tts_text += code.getDestinations().get(j);
            if(j < code.getDestinations().size()-1)
                tts_text += ", ";
        }
        Node node2 = getNode(2);
        TTSReading tts = (TTSReading) node2.getActions().get(3);
        tts.setTextToRead(tts_text);
    }

    // Fonction qui lit un texte ou un fichier mp3
    public void readTTSReader(TTSReading tts) {
        Log.v("fonction", "readTTSReader");
        if(!tts.getTextToRead().equals("")) {
            if (tts.getTextToRead().indexOf("https") != -1) {
                String path = FileDownloader.DOWNLOAD_PATH+(CompressionString.compress(tts.getTextToRead()))+".mp3";

                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    Log.v("Lecture", "Audio");
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Thread.sleep(mediaPlayer.getDuration());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                mainActivity.read(tts.getTextToRead());
            }
        }
    }

    // Fonction qui regarde si le correspondant à l'ID a bien été créé
    public boolean checkNodes(int nodeID){
        for(Node node : AllNodes){
            if(node.ID==nodeID) return true;
        }
        return false;
    }

    // Fonction qui retourne un noeud à partir de son ID
    public Node getNode(int nodeId){
        Log.v("fonction", "getNode");
        for(Node node : AllNodes){
            if(node.ID==nodeId)
                return node;
        }
        Log.d("Debug","Node "+nodeId+" non trouvé");
        return null;
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("Detection", "First detection");
    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        Log.v("Detection", "Next detection");
        if(detectedQR instanceof QRCodeReponseSeriousGame){
            this.reponseQR = (QRCodeReponseSeriousGame) detectedQR;
            String s = Integer.toString(current_node.ID);
            s += Integer.toString(code.idReponseQuestionQRCode(current_node.ID,reponseQR.getReponse()));
            int ID_reponse = Integer.parseInt(s);
            if(checkNodes(ID_reponse) ) {
                enigme(getNode(ID_reponse), getNode(ID_reponse));
            }
            mode_reponse = false;
        }
    }

    @Override
    public void onEndOfMultipleDetectionTimer() {

    }

    @Override
    public void onSwipeTop() {
        if(mainActivity.getDetectionProgress()!=MainActivity.NO_QR_DETECTED){
            if(code!=null){
                List<Action> actions = current_node.getActions();
                for(Action a : actions) {
                    if(a instanceof TTSReading) {
                        readTTSReader((TTSReading) a);
                        break;
                    }
                }
            }
        }
        else {
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
                m_mainActivity.startNewDetection("Scénario annulé, réactivation du mode normal. Nouvelle détection\"");
                hand.removeCallbacks(runner);
                posted = false;
                // On supprime le fichier xml du stockage interne
                supprimeFichierStockageInterne();
            }
        }
        else{
            ToneGeneratorSingleton.getInstance().errorTone();
        }
    }

    @Override
    public void onSwipeRight() {
        scan_reponse = true;
        mainActivity.makeSilence();
        mainActivity.readPrint("Détection de la réponse");
        mode_reponse = true;
    }

    @Override
    public void onSwipeLeft() {
        Log.v("swipe", "left");
        if(current_node.ID == 1) {
            // Pour attendre que le texte d'introduction soit lu en entier
            try {
                Thread.sleep(8000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites votre réponse...");
            try {
                ToneGeneratorSingleton.getInstance().lastQRCodeReadTone();
                m_mainActivity.startActivityForResult(intent, SPEECH_REQUEST_3);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_SHORT);
            }
            mode_reponse = false;
        } else {
            if(type_reponse.equals("speech")){
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites votre réponse...");
                try {
                    ToneGeneratorSingleton.getInstance().lastQRCodeReadTone();
                    m_mainActivity.startActivityForResult(intent, SPEECH_REQUEST_3);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_SHORT);
                }
                mode_reponse = false;
            }
        }
        type_reponse = "";
    }

    // Fonction qui en fonction de la réponse reçu par la reconnaissance vocale envoie sur la bonne énigme
    public void detectionDestination(){
        Log.v("fonction", "detectionAnswer");

        boolean destinationNonReconnu = true;
        // On parcours les destinations possibles
        for(int i = 0; i < code.getDestinations().size(); i++){
            // Si la réponse est égale à cette destination on lit le node correspondant
            Log.i("Debug_scenario","Comparaison reponse Speech : "+ this.reponseSpeech.toLowerCase()+" desti : "+code.getDestinations().get(i).toLowerCase());

            int id = code.idEnigme(code.getDestinations().get(i).toLowerCase());
            if(this.reponseSpeech.toLowerCase().equals(code.getDestinations().get(i).toLowerCase())){
                destinationNonReconnu = false;
                int newNode = 100+id;
                Log.i("Debug_scenario","On passe au node : "+newNode);
                readNode(newNode);
            }
        }
        if(destinationNonReconnu) readNode(100);
        if(mode_reponse == false){
            Log.i("Debug_scenario","On passe au node 2");
            readNode(2);
        }
    }

    @Override
    public void onDoubleClick() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    // Fonction qui récupère la réponse de la reconnaissance vocale
    public void setReponseSpeech(String reponse){
        Log.v("fonction", "setReponseSpeech");
        this.reponseSpeech = reponse;
        Log.i("Debug_scenario","Node id = "+ current_node.ID);
        if(current_node.ID == 2)
            detectionDestination();
        else if(current_node.ID > 2){
            Log.i("Debug_scenario","Passe dans le if");
            int ID_bonne_reponse = Integer.parseInt(current_node.ID+"1");
            int ID_mauvaise_reponse = Integer.parseInt(current_node.ID+"2");
            if(checkNodes(ID_bonne_reponse) && checkNodes(ID_mauvaise_reponse)){
                Log.i("Debug_scenario","Passe vers enigme");
                enigme(getNode(ID_bonne_reponse), getNode(ID_mauvaise_reponse));
            }

        }
    }

    private void supprimeFichierStockageInterne(){
        Log.i("Debug_scenario","Suppresion scenario");
        Log.i("Debug_scenario","Nombre de fichier dans storage interne "+ m_mainActivity.getApplicationContext().getFilesDir().listFiles().length);
        // Supprime le fichier scenario .xml
        m_mainActivity.getApplicationContext().deleteFile(code.getFILENAME());
        // On regarde si bien supprimé
        Log.i("Debug_scenario","Nombre de fichier dans storage interne "+ m_mainActivity.getApplicationContext().getFilesDir().listFiles().length);
        for(File f : m_mainActivity.getApplicationContext().getFilesDir().listFiles()){
            Log.i("Debug_scenario",f.getName());
        }
    }

    @Override
    public void onDownloadComplete() {
        Log.v("Download", "Complete");
    }

    public void onQRFileDownloadComplete() {
        //plays the newly downloaded sound
        m_mainActivity.playCurrentSoundFromFile();
    }
}
