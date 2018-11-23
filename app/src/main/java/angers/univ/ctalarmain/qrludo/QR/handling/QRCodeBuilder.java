package angers.univ.ctalarmain.qrludo.QR.handling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeEnsemble;
import angers.univ.ctalarmain.qrludo.QR.model.QrCodeJson;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;
import angers.univ.ctalarmain.qrludo.utils.DecompressionJSON;


/**
 * Created by Jules Leguy on 20/01/18.
 * Modified by Florian Lherbeil
 * Builds a QRCode object from a Json object
 */
public class QRCodeBuilder {

    public static QRCode build(String dataQR) throws UnhandledQRException {
        // On stocke la valeur brute initiale pour pouvoir effetuer la détection multiple
        String rawvalue=dataQR;

        // On vérfie si la chaine est encodée en base64
        if(dataQR.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")){
            // Start decompression
            try {
                dataQR=DecompressionJSON.decompresser(dataQR);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!dataQR.startsWith("{\"name\"")){
            // si la chaine trouvée n'est pas compressée et qu'elle n'est pas du json, c'est alors simplement du texte
            // On rajoute le texte dans une chaine json comportement les attributs type et data
            // Le texte est alors traité comme un qrcode atomique
            dataQR="{\"type\"=\"atomique\",\"data\"=[\""+dataQR+"\"]}";

        }

        Gson gson = new GsonBuilder().create();
        final QrCodeJson code = gson.fromJson(dataQR,QrCodeJson.class);
        if(code.getType().equalsIgnoreCase("atomique")){
            return new QRCodeAtomique(code,rawvalue);
        }
        else if (code.getType().equalsIgnoreCase("ensemble")){
            return new QRCodeEnsemble(code,rawvalue);
        }
        else
            return new QRCodeAtomique(code,rawvalue);


       /*QRCode builtQR = null;

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


        return builtQR;*/
    }


    /*public static QRCode buildQRCodeAtomiqueFromText(String dataQR) throws UnhandledQRException {

        //The QRCodeAtomique constructor creates the QRCodeAtomique from the raw string if it cannot parse xml
        return new QRCodeAtomique(dataQR);

    }*/


}
