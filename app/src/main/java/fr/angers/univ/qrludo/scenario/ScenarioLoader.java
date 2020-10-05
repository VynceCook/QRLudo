package fr.angers.univ.qrludo.scenario;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fr.angers.univ.qrludo.action.Action;
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

public class ScenarioLoader {
    MainActivity mainActivity;

    public ScenarioLoader(MainActivity mainactivity){
        mainActivity = mainactivity;
    }

    private ArrayList<Node> XMLScenario(MainActivity mainActivity) throws XmlPullParserException, IOException {
        InputStream in = mainActivity.getResources().openRawResource(mainActivity.getResources().getIdentifier("exemple_scenario_type", "raw", mainActivity.getPackageName()));

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readList(parser);
        } finally {
            in.close();
        }


    }

    private ArrayList<Node> readList(XmlPullParser parser) throws IOException, XmlPullParserException{
        ArrayList<Node> nodes = new ArrayList<Node>();

        parser.require(XmlPullParser.START_TAG, null, "liste");
        while(parser.next()!=XmlPullParser.END_TAG){
            String name = parser.getName();
            if(name.equals("node")) nodes.add(readNode(parser));
            else {
                throw new Error("Un tag différent d'un noeud a été trouvé");
            }
        }
        return nodes;
    }

    private Node readNode(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, null, "node");

        int ID = 0;
        ArrayList<Action> actions = new ArrayList<Action>();
        ArrayList<Atom> conditions =  new ArrayList<Atom>();

        while(parser.next() != XmlPullParser.END_TAG){
            String name = parser.getName();
            if(name.equals("id")){
                ID = readInt(parser, "id");
            }
            else if (name.equals("required_atoms")) {
                    conditions = readAtoms(parser);
            }
            else if(name.equals("action_list")) {
                actions = readActions(parser, ID);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "node");
        return new Node(mainActivity, ID, actions, conditions);
    }

    private int readInt(XmlPullParser parser, String tag) throws XmlPullParserException, IOException{

        parser.require(XmlPullParser.START_TAG, null, tag);
        int id = readNumber(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return id;
    }

    private int readNumber(XmlPullParser parser) throws XmlPullParserException, IOException{
        int result = 0;
        if (parser.next() == XmlPullParser.TEXT) {
            result = Integer.parseInt(parser.getText());
            parser.nextTag();
        }
        return result;
    }

    private ArrayList<Action> readActions(XmlPullParser parser, int ID) throws XmlPullParserException, IOException {
        ArrayList<Action> actions = new ArrayList<Action>();
        parser.require(XmlPullParser.START_TAG, null, "action_list");
        while(parser.next() != XmlPullParser.END_TAG){
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
            else if(name.equals("ClearNodes")){
                actions.add(new ClearNodes(mainActivity, ID));
            }
            else if(name.equals("VerificationConditionFinScenario")){
                actions.add(new VerificationConditionFinScenario(mainActivity, ID));
            }
            else if(name.equals("CaptureSpeech")){
                actions.add(new CaptureSpeech(mainActivity, ID));
            }
            else if(name.equals("ClearAtoms")){
                actions.add(new ClearAtoms(mainActivity, ID));
            }
            else if(name.equals("CaptureQR")){
                actions.add(new CaptureQR(mainActivity,ID));
            }
        }

        return actions;
    }

    private String readText(XmlPullParser parser, String tag) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, null, tag);
        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, tag);
        return text;
    }

    private ArrayList<Atom> readAtoms(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Atom> conditions = new ArrayList<Atom>();
        parser.require(XmlPullParser.START_TAG, null, "required_atoms");
        while(parser.next()!= XmlPullParser.END_TAG){
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

    private Atom readAtom(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "atom");
        String content ="";
        String type ="";
        while(parser.next()!=XmlPullParser.END_TAG){
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


}
