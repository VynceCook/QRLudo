package angers.univ.ctalarmain.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;

/**
 * Created by Jules Leguy on 04/02/18.
 * Modified by Florian Lherbeil
 */

public class QRCodeEnsemble extends QRCode{

    public QRCodeEnsemble(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        for(String data : code.getData()){
            if(isUrlFile(data)){
                m_content.add(new QRFile(data));
            }
            else {
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }
        }

        /*try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(rawValue)));
            Element rootNode = document.getDocumentElement();

            NodeList nodes = rootNode.getChildNodes();

            boolean contenuRead = false;

            for (int i=0; i<nodes.getLength(); i++){

                if (nodes.item(i).getNodeName().equals("contenu")){
                    readContent(nodes.item(i).getChildNodes());
                    contenuRead = true;
                }
            }

            if (!contenuRead){
                throw new UnhandledQRException("No <contenu> node");
            }
        }
        //The string cannot be parsed as XML
        catch (ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e) {
            throw new UnhandledQRException("Cannot parse XML");
        }*/


    }

    /**
     * Reads the content of the QRCodeEnsemble and inserts it into the attribute m_content of the superclass QRCode
     * @param content
     */
    private void readContent(NodeList content) throws UnhandledQRException {


        for (int i=0; i<content.getLength(); i++){

            if (content.item(i).getNodeName().equals("fichier")){
                QRFile qrFile = new QRFile(((Element)content.item(i)).getAttribute("url"));
                m_content.add(qrFile);
            }
            else if (content.item(i).getNodeName().equals("texte")){
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }

        }

    }

}
