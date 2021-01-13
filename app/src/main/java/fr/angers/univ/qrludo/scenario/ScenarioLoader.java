package fr.angers.univ.qrludo.scenario;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import fr.angers.univ.qrludo.action.Action;
import fr.angers.univ.qrludo.action.AddAtom;
import fr.angers.univ.qrludo.action.AddNode;
import fr.angers.univ.qrludo.action.CaptureQR;
import fr.angers.univ.qrludo.action.CaptureSpeech;
import fr.angers.univ.qrludo.action.ClearAtoms;
import fr.angers.univ.qrludo.action.ClearNodes;
import fr.angers.univ.qrludo.action.RemoveNode;
import fr.angers.univ.qrludo.action.TTSReading;
import fr.angers.univ.qrludo.action.VerificationConditionFinScenario;
import fr.angers.univ.qrludo.atom.Any;
import fr.angers.univ.qrludo.atom.Atom;
import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.atom.QRAtom;
import fr.angers.univ.qrludo.atom.SpeechAtom;

/**
 *  Cette classe permet de parser un fichier xml donné en des classes Java
 */

public class ScenarioLoader {
    MainActivity mainActivity;
    private String filename;

    public ScenarioLoader(MainActivity mainactivity, String filename){
        mainActivity = mainactivity;
        this.filename = filename;
    }

    // Cette fonction lance le parsing du fichier XML passé dans l'attribut filename dans le constructeur
    private ArrayList<Node> XMLScenario(MainActivity mainActivity) throws XmlPullParserException, IOException {
        //InputStream in = mainActivity.getResources().openRawResource(mainActivity.getResources().getIdentifier(filename, "raw", mainActivity.getPackageName()));
        FileInputStream fis = null;
        try {
            fis = mainActivity.openFileInput(filename);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            //parser.setInput(in, null);

            parser.setInput(fis, null);

            parser.nextTag();
            return readList(parser);
        } finally {
            //in.close();
            if(fis!= null){
                try {
                    fis.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }


    }

    // Cette fonction lit la première balise <node> et lance le parsing des sous-balises
    private ArrayList<Node> readList(XmlPullParser parser) throws IOException, XmlPullParserException{
        Log.v("fonction", "readList");
        ArrayList<Node> nodes = new ArrayList<Node>();

        parser.require(XmlPullParser.START_TAG, null, "liste");
        while(parser.next()!=XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if(name.equals("node")) {
                Log.v("node", "ajout d'un nouveau noeud");
                nodes.add(readNode(parser));
            }
            else {
                throw new Error("Un tag différent d'un noeud a été trouvé");
            }
        }
        return nodes;
    }

    // Cette fonction parsent les sous-balises de la balise <node>
    private Node readNode(XmlPullParser parser) throws XmlPullParserException, IOException{
        Log.v("fonction", "readNode");
        parser.require(XmlPullParser.START_TAG, null, "node");

        int ID = 0;
        ArrayList<Action> actions = new ArrayList<Action>();
        ArrayList<Atom> conditions =  new ArrayList<Atom>();

        while(parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if(name.equals("id")){
                Log.v("id", "lecture d'un id");
                ID = readInt(parser, "id");
            }
            else if (name.equals("required_atoms")) {
                Log.v("required", "atoms");
                conditions = readAtoms(parser);
            }
            else if(name.equals("action_list")) {
                Log.v("action", "list");
                actions = readActions(parser, ID);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "node");
        return new Node(mainActivity, ID, actions, conditions);
    }

    //Cette fonction permet de retourner la valeur d'une balise dont la valeur est un entier
    private int readInt(XmlPullParser parser, String tag) throws XmlPullParserException, IOException{
        Log.v("fonction", "readInt");

        parser.require(XmlPullParser.START_TAG, null, tag);
        int id = readNumber(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        Log.v("id", Integer.toString(id));
        return id;
    }

    // Cette fonction parse une chaîne de caractères dont la valeur est un entier
    private int readNumber(XmlPullParser parser) throws XmlPullParserException, IOException{
        Log.v("fonction", "readNumber");
        int result = 0;
        if (parser.next() == XmlPullParser.TEXT) {
            result = Integer.parseInt(parser.getText());
            parser.nextTag();
        }
        return result;
    }

    // Cette fonction parse la balise <action-list> et lance le parsing de ses sous-balises
    private ArrayList<Action> readActions(XmlPullParser parser, int ID) throws XmlPullParserException, IOException {
        Log.v("fonction", "readAction");

        ArrayList<Action> actions = new ArrayList<Action>();
        parser.require(XmlPullParser.START_TAG, null, "action_list");
        while(parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            if(name.equals("TTSReading")){
                actions.add(new TTSReading(mainActivity, ID, readText(parser, "TTSReading")));
            }
            else if(name.equals("RemoveNode")){
                actions.add(new RemoveNode(mainActivity, ID, readInt(parser, "RemoveNode")));
            }
            else if(name.equals("AddNode")){
                actions.add(new AddNode(mainActivity, ID, readInt(parser, "AddNode")));
            }
            else if(name.equals("AddAtom")){
                actions.add(new AddAtom(mainActivity, ID, readAtom(parser, "AddAtom")));
            }
            else if(name.equals("ClearNodes")){
                actions.add(new ClearNodes(mainActivity, ID));
                while (parser.next() != XmlPullParser.END_TAG);
            }
            else if(name.equals("VerificationConditionFinScenario")){
                actions.add(new VerificationConditionFinScenario(mainActivity, ID));
                while (parser.next() != XmlPullParser.END_TAG);
            }
            else if(name.equals("CaptureSpeech")){
                actions.add(new CaptureSpeech(mainActivity, ID));
                while (parser.next() != XmlPullParser.END_TAG);
            }
            else if(name.equals("ClearAtoms")){
                actions.add(new ClearAtoms(mainActivity, ID));
                while (parser.next() != XmlPullParser.END_TAG);
            }
            else if(name.equals("CaptureQR")){
                actions.add(new CaptureQR(mainActivity,ID));
                while (parser.next() != XmlPullParser.END_TAG);
            }
        }

        return actions;
    }

    // Cette fonction parse une balise dont la valeur est une chaîne de caractères
    private String readText(XmlPullParser parser, String tag) throws XmlPullParserException, IOException{
        Log.v("fonction", "readTexte");
        parser.require(XmlPullParser.START_TAG, null, tag);
        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            parser.nextTag();
        }
        Log.v("TTSReading", text);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return text;
    }

    // Cette fonction parse la balise <required_atoms> et lance le parsing de ses sous-balises
    private ArrayList<Atom> readAtoms(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.v("fonction", "readAtoms");
        ArrayList<Atom> conditions = new ArrayList<Atom>();
        parser.require(XmlPullParser.START_TAG, null, "required_atoms");
        while(parser.next()!= XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if(name.equals("atom")){
                conditions.add(readAtom(parser, "atom"));
            }
            else {
                throw new Error("Un tag différent d'un Atom a été trouvé");
            }
        }

        return conditions;
    }

    // Cette fonction parse la balise <atom> et lance le parsing de ses sous-balises
    private Atom readAtom(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        Log.v("fonction", "readAtom");
        parser.require(XmlPullParser.START_TAG, null, tag);
        String content ="";
        String type ="";
        while(parser.next()!=XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if(name.equals("content")){
                content = readText(parser,"content");
            }
            else if (name.equals("type")){
                type = readText(parser,"type");
            }
            else {
                throw new Error("Un tag non attendu a été trouvé, tag attendu : content,type");
            }
        }
        parser.require(XmlPullParser.END_TAG, null, tag);
        if(type.equals("Any")) return new Any(content);
        else if(type.equals("QRAtom")) return new QRAtom(content);
        else return new SpeechAtom(content);
    }

    public ArrayList<Node> getNodes() throws XmlPullParserException, IOException {
        return XMLScenario(mainActivity);
    }
}
