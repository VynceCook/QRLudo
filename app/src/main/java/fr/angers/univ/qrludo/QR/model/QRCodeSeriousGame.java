package fr.angers.univ.qrludo.QR.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fr.angers.univ.qrludo.activities.MainActivity;

/**
 *  Created by Simon Mitaty
 *  Implement by Pierre-Yves Delépine (29/10/2020)
 *  Le json représentant le QRCode scénario est transformé en un document xml
 */
public class QRCodeSeriousGame extends QRCode {

    //Texte d'introduction
    private String introduction = null;
    // Texte de fin
    private String fin = null;
    // Contient un tableau d'égnimes représentées sous la forme
    // [id énigme,nom énigme,type énigme]
    private ArrayList<Object> enigmes = new ArrayList<Object>();
    /*
     * Contient un tableau des questions de type RecoVocale sous la forme
     * [id énigme associé à la question, texte de la question, réponse de la question]
     */
    private ArrayList<Object> questionsRecoVocale = new ArrayList<Object>();
    /* Contient un tableau des questions de type QrCode sous la forme
     * [
     *  id énigme associé à la question,
     *  texte de la question ,
     *  [[texte de la réponse],[boolean pour savoir si c'est la bonne réponse]]
     * ]
     */
    private ArrayList<Object> questionsQrCode = new ArrayList<Object>();

    private String FILENAME = "scenario.xml";

    // Le fichier XML va être construit dans un DOM
    private Document doc;

    public QRCodeSeriousGame(QrCodeJson qr, String rawValue){
        super(qr,rawValue);

        Log.v("Qrcode", "SeriousGame");
        Gson gson = new GsonBuilder().create();
        QrCodeJsonSeriousGame codeScenario = gson.fromJson(rawValue, QrCodeJsonSeriousGame.class);

        introduction = codeScenario.getIntroduction();
        fin = codeScenario.getFin();
        enigmes = codeScenario.getEnigmes();
        questionsRecoVocale = codeScenario.getQuestionsRecoVocale();
        questionsQrCode = codeScenario.getQuestionsQrCode();

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element liste = doc.createElement("liste");
            liste.appendChild(createNodeIntroduction());
            liste.appendChild(createNodeAskDestination());
            liste.appendChild(createNodeFailRecoDestination());
            for(Element el : createNodeEnigme()){
                liste.appendChild(el);
            }
            for(Element el : createNodeReponse()){
                liste.appendChild(el);
            }
            liste.appendChild(createNodeEnigmeDejaResolue());
            liste.appendChild(createNodeFin());

            doc.appendChild(liste);

            prettyPrint(doc);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.i("Debug_scenario","Fin construction XML");

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }
    public void saveDocumentAsXMLFile(MainActivity mainActivity){
        // Sauvegarde le fichierxml dans le storage interne de l'appareil
        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = null;
        FileOutputStream fos = null;
        try {
            aTransformer = tranFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            fos = mainActivity.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            aTransformer.transform(src,new StreamResult(fos));
            Log.i("Debug_scenario","Document Save");


        } catch (TransformerException | FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i("Debug_scenario",""+mainActivity.getApplicationContext().getFilesDir().listFiles().length);
        for(File f : mainActivity.getApplicationContext().getFilesDir().listFiles()){
            Log.i("Debug_scenario",f.getName());
        }
    }

    public static final void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        System.out.println(out.toString());
    }

    private Element createNodeIntroduction(){

        Element node = doc.createElement("node");

        Element id = doc.createElement("id");
        id.insertBefore(doc.createTextNode("1"), id.getLastChild());

        Element required_atom = doc.createElement("required_atoms");
        Element action_list = doc.createElement("action_list");

        Element tts_node = doc.createElement("TTSReading");
        tts_node.insertBefore(doc.createTextNode(introduction), tts_node.getLastChild());

        Element removeNode = doc.createElement("RemoveNode");
        removeNode.insertBefore(doc.createTextNode("1"), removeNode.getLastChild());

        Element addNode = doc.createElement("AddNode");
        addNode.insertBefore(doc.createTextNode("2"), addNode.getLastChild());

        action_list.appendChild(tts_node);
        action_list.appendChild(removeNode);
        action_list.appendChild(addNode);

        node.appendChild(id);
        node.appendChild(required_atom);
        node.appendChild(action_list);

        return node;
    }

    private Element createNodeFin(){
        Element node = doc.createElement("node");

        Element id = doc.createElement("id");
        id.insertBefore(doc.createTextNode("105"), id.getLastChild());

        Element required_atom = doc.createElement("required_atoms");

        Element action_list = doc.createElement("action_list");

        Element tts_node = doc.createElement("TTSReading");
        tts_node.insertBefore(doc.createTextNode(fin), tts_node.getLastChild());

        Element clearAtom = doc.createElement("ClearAtoms");
        clearAtom.insertBefore(doc.createTextNode("SpeechAtom"), clearAtom.getLastChild());

        Element clearNode = doc.createElement("ClearNodes");

        action_list.appendChild(clearAtom);
        action_list.appendChild(clearNode);
        action_list.appendChild(tts_node);

        node.appendChild(id);
        node.appendChild(required_atom);
        node.appendChild(action_list);

        return node;
    }

    private Element createNodeAskDestination(){
        Element node = doc.createElement("node");

        Element id = doc.createElement("id");
        id.insertBefore(doc.createTextNode("2"), id.getLastChild());
        node.appendChild(id);

        Element required_atom = doc.createElement("required_atoms");
        node.appendChild(required_atom);

        Element action_list = doc.createElement("action_list");

        Element clearNode = doc.createElement("ClearNodes");
        action_list.appendChild(clearNode);

        Element addNode = doc.createElement("AddNode");
        addNode.insertBefore(doc.createTextNode("105"), addNode.getLastChild());
        action_list.appendChild(addNode);

        Element verif = doc.createElement("VerificationConditionFinScenario");
        action_list.appendChild(verif);

        Element tts_node = doc.createElement("TTSReading");
        String tts_text = "Choisis une destination ! Parmi, ";
        String[] ids;
        ids = new String[3];
        int i = 0;
        String destinations = "";
        for(Object enigmeObj : enigmes){
            ArrayList enigme = (ArrayList) enigmeObj;

            destinations += enigme.get(1).toString()+", ";
            ids[i] = enigme.get(0).toString();
            i++;
        }
        tts_text += destinations ;
        tts_node.insertBefore(doc.createTextNode(tts_text), tts_node.getLastChild());
        action_list.appendChild(tts_node);

        Element addNode100 = doc.createElement("AddNode");
        addNode100.insertBefore(doc.createTextNode("100"), addNode100.getLastChild());
        action_list.appendChild(addNode100);

        for(String id_node : ids){
            Element addNodes = doc.createElement("AddNode");
            addNodes.insertBefore(doc.createTextNode("10"+id_node), addNodes.getLastChild());
            action_list.appendChild(addNodes);
        }

        Element captureSpeech = doc.createElement("CaptureSpeech");
        action_list.appendChild(captureSpeech);

        node.appendChild(action_list);

        return node;
    }

    private Element createNodeFailRecoDestination(){
        Element node = doc.createElement("node");

        Element id = doc.createElement("id");
        id.insertBefore(doc.createTextNode("100"), id.getLastChild());
        node.appendChild(id);

        Element required_atom = doc.createElement("required_atoms");
        Element atom = doc.createElement("atom");
        Element type = doc.createElement("type");
        type.insertBefore(doc.createTextNode("Any"), type.getLastChild());
        atom.appendChild(type);
        Element content = doc.createElement("content");
        content.insertBefore(doc.createTextNode("Any"), content.getLastChild());
        atom.appendChild(content);

        required_atom.appendChild(atom);
        node.appendChild(required_atom);

        Element action_list = doc.createElement("action_list");

        action_list.appendChild(doc.createElement("ClearNodes"));

        Element tts_node = doc.createElement("TTSReading");
        tts_node.insertBefore(doc.createTextNode("Destination non reconnue"), tts_node.getLastChild());
        action_list.appendChild(tts_node);

        Element clearAtom = doc.createElement("ClearAtoms");
        clearAtom.insertBefore(doc.createTextNode("SpeechAtom"), clearAtom.getLastChild());
        action_list.appendChild(clearAtom);

        Element addNode = doc.createElement("AddNode");
        addNode.insertBefore(doc.createTextNode("2"), addNode.getLastChild());
        action_list.appendChild(addNode);

        node.appendChild(action_list);

        return node;
    }

    private Element createNodeEnigmeDejaResolue(){
        Element node = doc.createElement("node");

        Element id = doc.createElement("id");
        id.insertBefore(doc.createTextNode("104"), id.getLastChild());
        node.appendChild(id);

        node.appendChild(doc.createElement("required_atoms"));

        Element action_list = doc.createElement("action_list");

        Element clearAtom = doc.createElement("ClearAtoms");
        clearAtom.insertBefore(doc.createTextNode("SpeechAtom"), clearAtom.getLastChild());
        action_list.appendChild(clearAtom);

        action_list.appendChild(doc.createElement("ClearNodes"));

        Element tts_node = doc.createElement("TTSReading");
        tts_node.insertBefore(doc.createTextNode("Tu as déjà résolue cette énigme"), tts_node.getLastChild());
        action_list.appendChild(tts_node);

        Element addNode = doc.createElement("AddNode");
        addNode.insertBefore(doc.createTextNode("2"), addNode.getLastChild());
        action_list.appendChild(addNode);

        node.appendChild(action_list);

        return node;
    }

    private ArrayList<Element> createNodeEnigme(){
        ArrayList<Element> nodes = new ArrayList<>();
        for(Object enigmeObj : enigmes){
            ArrayList enigme = (ArrayList) enigmeObj;

            Element node = doc.createElement("node");
            Element id = doc.createElement("id");
            id.insertBefore(doc.createTextNode("10"+enigme.get(0).toString()), id.getLastChild());
            node.appendChild(id);

            Element required_atom = doc.createElement("required_atoms");
            Element atom = doc.createElement("atom");
            Element type = doc.createElement("type");
            type.insertBefore(doc.createTextNode("SpeechAtom"), type.getLastChild());
            atom.appendChild(type);
            Element content = doc.createElement("content");
            content.insertBefore(doc.createTextNode(enigme.get(1).toString()), content.getLastChild());
            atom.appendChild(content);

            required_atom.appendChild(atom);
            node.appendChild(required_atom);

            Element action_list = doc.createElement("action_list");

            Element clearAtom = doc.createElement("ClearAtoms");
            clearAtom.insertBefore(doc.createTextNode("SpeechAtom"), clearAtom.getLastChild());
            action_list.appendChild(clearAtom);

            action_list.appendChild(doc.createElement("ClearNodes"));

            Element tts_node = doc.createElement("TTSReading");
            String tts_text = "Bienvenue dans "+enigme.get(1).toString()+" ! Répondez à la question suivante : ";

            // Test le type d'énigme pour aller chercher la question dans l'array correspoondant
            // Énigme type QRCode
            if(enigme.get(2).toString().equals("questionQRCode")){
                // On parcours les questions QRCode à la recherche de celle avec le bonne id
                for(Object questionObj : questionsQrCode){
                    ArrayList questionQRCode = (ArrayList) questionObj;
                    if(questionQRCode.get(0).toString().equals(enigme.get(0).toString())){
                        // Si c'est le bon id on ajoute la question
                        tts_text += questionQRCode.get(1).toString();
                        tts_node.insertBefore(doc.createTextNode(tts_text), tts_node.getLastChild());
                        action_list.appendChild(tts_node);

                        Element captureQr = doc.createElement("CaptureQR");
                        action_list.appendChild(captureQr);
                        // Ajout node de réponse
                        ArrayList reponses = (ArrayList) questionQRCode.get(2);
                        for(int i = 0; i <(int) reponses.size(); i++){
                            Element addNodes = doc.createElement("AddNode");
                            addNodes.insertBefore(doc.createTextNode("10"+enigme.get(0).toString()+(i+1)), addNodes.getLastChild());
                            action_list.appendChild(addNodes);
                        }
                    }
                }
                // Énigme type RecoVocale
            }else if(enigme.get(2).toString().equals("questionRecoVocale")){
                // On parcours les questions à RecoVocal à la recherche de celle avec le bonne id
                for(Object questionObj : questionsRecoVocale){
                    ArrayList questionRecoVocal = (ArrayList) questionObj;
                    if(questionRecoVocal.get(0).toString().equals(enigme.get(0).toString())){
                        // Si c'est le bon id on ajoute la question
                        tts_text += questionRecoVocal.get(1).toString();
                        tts_node.insertBefore(doc.createTextNode(tts_text), tts_node.getLastChild());
                        action_list.appendChild(tts_node);

                        Element captureSpeech = doc.createElement("CaptureSpeech");
                        action_list.appendChild(captureSpeech);

                        // Ajout node de réponse
                        // 1 : bonne réponse 2 : mauvaise réponses
                        Element addNode1 = doc.createElement("AddNode");
                        addNode1.insertBefore(doc.createTextNode("10"+enigme.get(0).toString()+"1"), addNode1.getLastChild());
                        action_list.appendChild(addNode1);

                        Element addNode2 = doc.createElement("AddNode");
                        addNode2.insertBefore(doc.createTextNode("10"+enigme.get(0).toString()+"2"), addNode2.getLastChild());
                        action_list.appendChild(addNode2);
                    }
                }
            }
            node.appendChild(action_list);
            nodes.add(node);
        }
        return nodes;
    }

    private ArrayList<Element> createNodeReponse(){
        ArrayList<Element> nodes = new ArrayList<>();
        // Création des nodes des réponses des questions QRCode
        for(Object questionObj : questionsQrCode){
            ArrayList questionQRCode = (ArrayList) questionObj;
            ArrayList reponses = (ArrayList) questionQRCode.get(2);
            for(int i = 0; i <(int) reponses.size(); i++){
                Element node = doc.createElement("node");
                Element id = doc.createElement("id");
                id.insertBefore(doc.createTextNode("10"+questionQRCode.get(0).toString()+(i+1)), id.getLastChild());
                node.appendChild(id);

                Element required_atom = doc.createElement("required_atoms");

                Element atom = doc.createElement("atom");
                Element type = doc.createElement("type");
                type.insertBefore(doc.createTextNode("QRAtom"), type.getLastChild());
                atom.appendChild(type);
                Element content = doc.createElement("content");
                ArrayList reponse = (ArrayList) reponses.get(i);
                content.insertBefore(doc.createTextNode(reponse.get(0).toString()), content.getLastChild());
                atom.appendChild(content);
                required_atom.appendChild(atom);

                node.appendChild(required_atom);

                Element action_list = doc.createElement("action_list");

                Element clearAtom = doc.createElement("ClearAtoms");
                clearAtom.insertBefore(doc.createTextNode("QRAtom"), clearAtom.getLastChild());
                action_list.appendChild(clearAtom);

                action_list.appendChild(doc.createElement("ClearNodes"));

                Element tts_node = doc.createElement("TTSReading");
                String tts_text = "Énigme de  ";

                for(Object enigmeObj : enigmes) {
                    ArrayList enigme = (ArrayList) enigmeObj;
                    if(enigme.get(0).toString().equals(questionQRCode.get(0).toString())){
                        tts_text += enigme.get(1).toString();
                    }
                }
                if(reponse.get(1).equals("true")){
                    tts_text += " résolue";
                    tts_node.insertBefore(doc.createTextNode(tts_text), tts_node.getLastChild());
                    action_list.appendChild(tts_node);

                    Element addAtom = doc.createElement("AddAtom");
                    type = doc.createElement("type");
                    type.insertBefore(doc.createTextNode("EnigmeAtom"), type.getLastChild());
                    addAtom.appendChild(type);
                    content = doc.createElement("content");
                    content.insertBefore(doc.createTextNode("10"+questionQRCode.get(0).toString()), content.getLastChild());
                    addAtom.appendChild(content);

                    action_list.appendChild(addAtom);
                }else if(reponse.get(1).equals("false")){
                    tts_text += " non résolue";
                    tts_node.insertBefore(doc.createTextNode(tts_text), tts_node.getLastChild());
                    action_list.appendChild(tts_node);
                }
                Element addNode = doc.createElement("AddNode");
                addNode.insertBefore(doc.createTextNode("2"), addNode.getLastChild());
                action_list.appendChild(addNode);

                node.appendChild(action_list);
                nodes.add(node);
            }
        }
        // Création des nodes des réponses des questions RecoVocal
        for(Object questionObj : questionsRecoVocale){
            ArrayList questionRecoVocal = (ArrayList) questionObj;
            //Ajout bonne réponse id = 10 + questionRecoVocal.get(0).toString() + 1
            Element node = doc.createElement("node");
            Element id = doc.createElement("id");
            id.insertBefore(doc.createTextNode("10"+questionRecoVocal.get(0).toString()+1), id.getLastChild());
            node.appendChild(id);

            Element required_atom = doc.createElement("required_atoms");
            Element atom = doc.createElement("atom");
            Element type = doc.createElement("type");
            type.insertBefore(doc.createTextNode("SpeechAtom"), type.getLastChild());
            atom.appendChild(type);
            Element content = doc.createElement("content");
            content.insertBefore(doc.createTextNode(questionRecoVocal.get(2).toString()), content.getLastChild());
            atom.appendChild(content);
            required_atom.appendChild(atom);

            node.appendChild(required_atom);

            Element action_list = doc.createElement("action_list");

            Element clearAtom = doc.createElement("ClearAtoms");
            clearAtom.insertBefore(doc.createTextNode("SpeechAtom"), clearAtom.getLastChild());
            action_list.appendChild(clearAtom);

            action_list.appendChild(doc.createElement("ClearNodes"));

            Element tts_node = doc.createElement("TTSReading");
            String tts_text = "Énigme de  ";

            for(Object enigmeObj : enigmes) {
                ArrayList enigme = (ArrayList) enigmeObj;
                if(enigme.get(0).toString().equals(questionRecoVocal.get(0).toString())){
                    tts_text += enigme.get(1).toString();
                }
            }
            tts_node.insertBefore(doc.createTextNode(tts_text+" résolue"), tts_node.getLastChild());
            action_list.appendChild(tts_node);

            Element addAtom1 = doc.createElement("AddAtom");
            type = doc.createElement("type");
            type.insertBefore(doc.createTextNode("EnigmeAtom"), type.getLastChild());
            addAtom1.appendChild(type);
            content = doc.createElement("content");
            content.insertBefore(doc.createTextNode("10"+questionRecoVocal.get(0).toString()), content.getLastChild());
            addAtom1.appendChild(content);
            action_list.appendChild(addAtom1);

            Element addNode = doc.createElement("AddNode");
            addNode.insertBefore(doc.createTextNode("2"), addNode.getLastChild());
            action_list.appendChild(addNode);

            node.appendChild(action_list);
            nodes.add(node);

            //Ajout mauvaise réponse id = 10 + questionRecoVocal.get(0).toString() + 2

            node = doc.createElement("node");
            id = doc.createElement("id");
            id.insertBefore(doc.createTextNode("10"+questionRecoVocal.get(0).toString()+2), id.getLastChild());
            node.appendChild(id);

            required_atom = doc.createElement("required_atoms");
            atom = doc.createElement("atom");
            type = doc.createElement("type");
            type.insertBefore(doc.createTextNode("Any"), type.getLastChild());
            atom.appendChild(type);
            content = doc.createElement("content");
            content.insertBefore(doc.createTextNode("SpeechAtom"), content.getLastChild());
            atom.appendChild(content);
            required_atom.appendChild(atom);

            node.appendChild(required_atom);

            action_list = doc.createElement("action_list");

            clearAtom = doc.createElement("ClearAtoms");
            clearAtom.insertBefore(doc.createTextNode("SpeechAtom"), clearAtom.getLastChild());
            action_list.appendChild(clearAtom);

            action_list.appendChild(doc.createElement("ClearNodes"));

            tts_node = doc.createElement("TTSReading");
            tts_text = "Énigme de  ";

            for(Object enigmeObj : enigmes) {
                ArrayList enigme = (ArrayList) enigmeObj;
                if(enigme.get(0).toString().equals(questionRecoVocal.get(0).toString())){
                    tts_text += enigme.get(1).toString();
                }
            }

            tts_node.insertBefore(doc.createTextNode(tts_text+" non résolue"), tts_node.getLastChild());
            action_list.appendChild(tts_node);

            action_list.appendChild(addNode);

            node.appendChild(action_list);
            nodes.add(node);
        }
        return nodes;
    }

    public Document getDoc() {
        return doc;
    }

    public String getFILENAME() {
        return FILENAME;
    }

    public void setFILENAME(String FILENAME) {
        this.FILENAME = FILENAME;
    }
}
