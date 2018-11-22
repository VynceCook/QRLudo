package angers.univ.ctalarmain.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;
import angers.univ.ctalarmain.qrludo.utils.FileDowloader;

/**
 * Created by Jules Leguy on 20/01/18.
 *
 */


/**
 * Represents the information of a QRCodeAtomique. It can belong to a family or not.
 */
public class QRCodeAtomique extends QRCode {

    private boolean m_belongsToFamily = false;
    private String m_familyName = "";
    private int m_familyRank = -1;

    /**
     * Constructor that builds the object from a string.
     * The string is in Json
     *
     * @param code
     */
    public QRCodeAtomique(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        System.out.println(code);

        boolean contenuRead = false;

        for(String data : code.getData()){
            if(isUrlFile(data)){
                m_content.add(new QRFile(data));
            }
            else {
                m_content.add(new QRText(data));
            }
        }
        /*try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(rawValue)));
            Element rootNode = document.getDocumentElement();

            NodeList nodes = rootNode.getChildNodes();

            //boolean contenuRead = false;

            for (int i=0; i<nodes.getLength(); i++){

                if (nodes.item(i).getNodeName().equals("contenu")){
                    readContent(nodes.item(i).getChildNodes());
                    contenuRead = true;
                }
                else if (nodes.item(i).getNodeName().equals("famille")){
                    readFamilyInfos(nodes.item(i));
                }
            }

            if (!contenuRead){
                throw new UnhandledQRException("No <contenu> node");
            }
        }
        //The string cannot be parsed as XML so creating the QRCodeAtomique from the raw string
        catch (ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e) {

            //If the text represents an file on google drive
            if (rawValue.startsWith("https://drive.google.com")){

                // fetching the id of the google drive file in the raw string
                String id = rawValue.substring(rawValue.indexOf("id=")+3);

                Log.v("test", "id fichier : "+id);

                //Adding a QRFile to the QRCode
                m_content.add(new QRFile(id));
            }
            //The text is interpreted as it is
            else{
                m_content.add(new QRText(rawValue));
            }

        }*/


    }



    /**
     * Reads the content of the QRCodeAtomique and inserts it into the attribute m_content of the superclass QRCode
     * @param content
     */
    private void readContent(NodeList content){


        for (int i=0; i<content.getLength(); i++){

            if (content.item(i).getNodeName().equals("fichier")){
                m_content.add(new QRFile(((Element)content.item(i)).getAttribute("url")));
            }
            else if (content.item(i).getNodeName().equals("texte")){
                //If the text represents an file on google drive
                if (content.item(i).getTextContent().startsWith("https://drive.google.com")){

                    // fetching the id of the google drive file in the raw string
                    String id = content.item(i).getTextContent().substring(content.item(i).getTextContent().indexOf("id=")+3);

                    Log.v("test", "id fichier : "+id);

                    //Adding a QRFile to the QRCode
                    m_content.add(new QRFile(id));
                }else {
                    m_content.add(new QRText(content.item(i).getTextContent()));
                }
            }

        }

    }

    /**
     * If the QRCode belongs to a family, records the family information
     * @param familyInfo
     */
    private void readFamilyInfos(Node familyInfo){

        m_belongsToFamily = true;
        m_familyName = ((Element)familyInfo).getAttribute("nom");
        m_familyRank = Integer.valueOf(((Element)familyInfo).getAttribute("ordre"));

    }

    /**
     * Returns true if the QRCodeAtomique belongs to a family
     * @return
     */
    public boolean belongsToFamily(){
        return m_belongsToFamily;
    }

    /**
     * Returns the rank of a QRCodeAtomique in its family or throws a FamilyException if it doesn't belong to a family
     *
     * @return
     * @throws FamilyException
     */
    public int getFamilyRank() throws FamilyException {
        if (m_belongsToFamily){
            return m_familyRank;
        }
        else{
            throw new FamilyException();
        }
    }

    /**
     *  Returns the name of the QRCodeAtomique's family or throws a FamilyException if it doesn't belong to a family
     *
     * @return
     * @throws FamilyException
     */
    public String getFamilyName() throws FamilyException {

        if (m_belongsToFamily){
            return m_familyName;
        }
        else{
            throw new FamilyException();
        }

    }


}
