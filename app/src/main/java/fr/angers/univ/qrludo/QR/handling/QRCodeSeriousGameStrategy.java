package fr.angers.univ.qrludo.QR.handling;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QRCodeSeriousGame;
import fr.angers.univ.qrludo.action.Action;
import fr.angers.univ.qrludo.action.AddNode;
import fr.angers.univ.qrludo.action.CaptureSpeech;
import fr.angers.univ.qrludo.action.ClearAtoms;
import fr.angers.univ.qrludo.action.ClearNodes;
import fr.angers.univ.qrludo.action.RemoveNode;
import fr.angers.univ.qrludo.action.TTSReading;
import fr.angers.univ.qrludo.action.VerificationConditionFinScenario;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.atom.Atom;
import fr.angers.univ.qrludo.scenario.Node;
import fr.angers.univ.qrludo.scenario.ScenarioLoader;
import fr.angers.univ.qrludo.utils.ToneGeneratorSingleton;

public class QRCodeSeriousGameStrategy extends QRCodeDetectionModeStrategy {

    private ScenarioLoader scenario;
    private ArrayList<Node> AllNodes, OpenNodes, RemoveNode;
    private QRCodeSeriousGame code;
    private MainActivity mainActivity;
    private TextToSpeech text;
    private boolean scan_reponse = false;
    private boolean mode_reponse = false;
    private boolean firstDetection = true;
    private String reponseSpeech;

    public QRCodeSeriousGameStrategy(MainActivity mainActivity, QRCodeSeriousGame code, TextToSpeech text){
        super(mainActivity);
        this.code = code;
        this.mainActivity = mainActivity;
        this.scenario = new ScenarioLoader(mainActivity,"/res/raw/exemple_scenario_type.xml");
        this.text = text;
        try {
            this.AllNodes = scenario.getNodes();
        } catch (IOException e){
            e.printStackTrace();
        } catch (XmlPullParserException e){
            e.printStackTrace();
        }
    }

    public void readCondition(List<Atom> conditions){

    }

    public void doActions(List<Action> actions){
        for(Action a : actions){
            if(a instanceof TTSReading){
                readTTSReader((TTSReading) a);
            }
            else if(a instanceof RemoveNode) {
                removeNode((RemoveNode) a);
            }
            else if(a instanceof AddNode) {
                OpenNodes.add(getNode(((AddNode) a).getNodeToAddID()));
            }
            else if(a instanceof ClearNodes){
                RemoveNode.add(getNode(((RemoveNode) a).getNodeToAddID()));

            }
            else if(a instanceof VerificationConditionFinScenario){

            }
            else if(a instanceof ClearAtoms){

            }
            else if(a instanceof CaptureSpeech){

            }
            OpenNodes.removeAll(RemoveNode);
        }
    }

    public void readTTSReader(TTSReading tts){
        text = new TextToSpeech(mainActivity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                text.setLanguage(Locale.FRENCH);
            }
        });
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
        UtteranceProgressListener progressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d("Debug2","Start");
            }

            @Override
            public void onDone(String utteranceId) {
                Log.d("Debug2","Reading");
            }

            @Override
            public void onError(String utteranceId) {

            }
        };
        text.setOnUtteranceProgressListener(progressListener);
        text.speak(tts.getTextToRead(), TextToSpeech.QUEUE_FLUSH, map);
    }

    public boolean checkNodes(int nodeID){
        for(Node node : AllNodes){
            if(node.ID==nodeID) return true;
        }
        return false;
    }

    public void removeNode(RemoveNode removeNode){
        for(Node node : OpenNodes){
            if(node.ID==removeNode.getNodeToAddID())
                OpenNodes.remove(node);
        }
    }

    public Node getNode(int nodeId){
        for(Node node : AllNodes){
            if(node.ID==nodeId)
                return node;
        }
        Log.d("Debug2","Node "+nodeId+" non trouvé");
        return null;
    }

    @Override
    public void onFirstDetectionWithTimeNotNull(QRCode detectedQR) {

    }

    @Override
    public void onNextDetectionWithTimeNotNull(QRCode detectedQR) {
        Node current_node;
        if(firstDetection){
            current_node = getNode(1);
            OpenNodes.add(current_node);
            if(current_node!=null){
                List<Atom> conditions = current_node.getConditions();
                List<Action> actions = current_node.getActions();
                if(conditions.size()>0){
                    readCondition(conditions);
                }
                if(actions.size()>0){
                    doActions(actions);
                }
            }
            firstDetection = false;
        }
        else {
            current_node = OpenNodes.get(0);
            if(current_node!=null) {
                List<Atom> conditions = current_node.getConditions();
                List<Action> actions = current_node.getActions();
                if(conditions.size()>0){
                    readCondition(conditions);
                }
                if(actions.size()>0){
                    doActions(actions);
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
        if(mainActivity.getDetectionProgress()!=MainActivity.NO_QR_DETECTED){
            if(code!=null){
                List<Action> actions = OpenNodes.get(0).getActions();
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
        scan_reponse = true;
        mainActivity.makeSilence();
        mainActivity.readPrint("Détection de la réponse");
        mode_reponse = true;
    }

    @Override
    public void onSwipeRight() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Dites votre réponse...");
        try{
            m_mainActivity.startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a){
            Toast.makeText(m_mainActivity.getApplicationContext(), "Désolé ! La reconnaissance vocale n'est pas supportée sur cet appareil.", Toast.LENGTH_SHORT);
        }
        mode_reponse = false;
    }

    @Override
    public void onDoubleClick() {
        ToneGeneratorSingleton.getInstance().errorTone();
    }

    public void setReponseSpeech(String reponse){
        this.reponseSpeech = reponse;
    }
}
