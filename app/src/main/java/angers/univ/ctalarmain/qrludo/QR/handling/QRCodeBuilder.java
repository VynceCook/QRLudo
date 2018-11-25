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
        String rawvalue = dataQR;

        // On vérfie si la chaine est encodée en base64
        if (dataQR.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) {
            // Début de la décompression
            try {
                dataQR = DecompressionJSON.decompresser(dataQR);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!dataQR.startsWith("{\"name\"")) {
            // Si la chaine trouvée n'est pas compressée et qu'elle n'est pas du json, c'est alors simplement du texte
            // On rajoute le texte dans une chaine json comportement les attributs type et data
            // Le texte est alors traité comme un qrcode atomique
            dataQR = "{\"type\"=\"atomique\",\"data\"=[\"" + dataQR + "\"]}";

        }

        Gson gson = new GsonBuilder().create();
        final QrCodeJson code = gson.fromJson(dataQR, QrCodeJson.class);
        // Si le json est trop gros et stocké sur internet
        if (code.getType().equalsIgnoreCase("file")) {
            // Téléchargement du fichier
            /*try {
                dataQR=DecompressionJSON.decompresser(dataQR);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        if (code.getType().equalsIgnoreCase("atomique")||code.getType().equalsIgnoreCase("unique")||code.getType().equalsIgnoreCase("xl")) {
            return new QRCodeAtomique(code, rawvalue);
        } else if (code.getType().equalsIgnoreCase("ensemble")) {
            return new QRCodeEnsemble(code, rawvalue);
        } else
            return new QRCodeAtomique(code, rawvalue);


    }


}
