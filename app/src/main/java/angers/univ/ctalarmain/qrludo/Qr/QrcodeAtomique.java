package angers.univ.ctalarmain.qrludo.Qr;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.channels.FileLockInterruptionException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Created by etudiant on 16/11/17.
 */

public class QrcodeAtomique extends Qrcode {

    /**
     * Contenu je recupére le noeud contenu qui est le pére de tous des noeuds textes
     */
    String contenu;

    /**
     * texte contient tous les contenus des champs texte.
     */
    ArrayList<String> texte;

    public QrcodeAtomique(String xmlData) {
        super(xmlData);
        texte = new ArrayList<String>();
        parseXML();
    }


    public void setTexte(ArrayList<String> value) {
        texte = value;
    }

    public ArrayList<String> getTexte() {
        return texte;
    }

    public void parseXML() {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(getDataQrcode())));
            Element racine = document.getDocumentElement();
            NodeList contenu = racine.getChildNodes();
            NodeList childOfContenu = contenu.item(0).getChildNodes();
            for (int i = 0; i < childOfContenu.getLength(); i++) {
                if (childOfContenu.item(i).getNodeName().equals("texte"))
                    texte.add(childOfContenu.item(i).getTextContent());
                else if (childOfContenu.item(i).getNodeName().equals("fichier")) {
                    NamedNodeMap attrFichier = childOfContenu.item(i).getAttributes();
                    String nom = attrFichier.item(0).getTextContent();
                    String url = attrFichier.item(1).getTextContent();
                    Fichier fichier = new Fichier(nom, url);
                }

            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}