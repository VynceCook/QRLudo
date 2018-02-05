package angers.univ.ctalarmain.qrludo.QR.handling;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeEnsemble;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;


/**
 * Created by Jules Leguy on 20/01/18.
 * Builds a QRCode object from a XML object
 */
public class QRCodeBuilder {

    public static QRCode build(String dataQR) throws UnhandledQRException {

        QRCode builtQR = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(dataQR)));
            Element root = document.getDocumentElement();

            //Checking if the root element has the right name
            if (root.getNodeName().equals("donneesutilisateur")){

                //Creating the right QRCode object depending on its kind
                if (root.getAttribute("type").equals("atomique")){
                    builtQR = new QRCodeAtomique(dataQR);                }
                else if (root.getAttribute("type").equals("ensemble")){
                    builtQR = new QRCodeEnsemble(dataQR);
                }
                else{
                    throw new UnhandledQRException("Unknown QR type");
                }


            }
            else{
                throw new UnhandledQRException("Invalid root element");
            }

        } catch (Exception e) {
            //The XML cannot be parsed so building a QRCodeAtomique from the raw text
            e.printStackTrace();
            builtQR = buildQRCodeAtomiqueFromText(dataQR);
        }


        return builtQR;
    }

    /**
     * Builds a QRCode object from a non-xml string
     * Allows backward compatibility with the QRCodes created for the first version of the app
     * Builds a QRCodeAtomique with a single QRContent, which can be a QRText or a QRFile depending on the string
     *
     * @param dataQR
     * @return
     */
    public static QRCode buildQRCodeAtomiqueFromText(String dataQR) throws UnhandledQRException {

        //The QRCodeAtomique constructor creates the QRCodeAtomique from the raw string if it cannot parse xml
        return new QRCodeAtomique(dataQR);

    }


}
