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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import angers.univ.ctalarmain.qrludo.FichierDejaExistantException;

/**
 * Created by etudiant on 29/11/17.
 */

public class QrcodeEnsemble extends Qrcode {

    ArrayList<Fichier> listesDesFichiers;

    public QrcodeEnsemble(String xmlData) {
        super(xmlData);
        //parseXML();
    }

    public ArrayList<Fichier> getListeFichier() {
        return listesDesFichiers;
    }

    public void parseXML() {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(getDataQrcode())));
            Element racine = document.getDocumentElement();
            NodeList contenu = racine.getChildNodes();
            NodeList childOfContenu = contenu.item(0).getChildNodes();
            for (int i = 0; i < childOfContenu.getLength(); i++)
                if (childOfContenu.item(i).getNodeName().equals("fichier")) {
                    NamedNodeMap attrFichier = childOfContenu.item(i).getAttributes();
                    String nom = attrFichier.item(0).getTextContent();
                    String url = attrFichier.item(1).getTextContent();
                    Fichier fichier = new Fichier(nom, url);
                    listesDesFichiers.add(fichier);
                }

        }
        catch (ParserConfigurationException e1){
            e1.printStackTrace();
        }
        catch (IOException e1){
            e1.printStackTrace();
        }
        catch (SAXException e1){
            e1.printStackTrace();
        }
    }
}