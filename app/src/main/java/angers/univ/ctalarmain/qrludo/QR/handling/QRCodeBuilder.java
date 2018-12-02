package angers.univ.ctalarmain.qrludo.QR.handling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import angers.univ.ctalarmain.qrludo.QR.model.FileJson;
import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeAtomique;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeEnsemble;
import angers.univ.ctalarmain.qrludo.QR.model.QrCodeJson;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;
import angers.univ.ctalarmain.qrludo.utils.DecompressionJSON;
import angers.univ.ctalarmain.qrludo.utils.JSONDownloader;


/**
 * Created by Jules Leguy on 20/01/18.
 * Modified by Florian Lherbeil
 * Builds a QRCode object from a Json object
 */
public class QRCodeBuilder {

    private String result;

    public static QRCode build(String dataQR) throws UnhandledQRException {
        // On stocke la valeur brute initiale pour pouvoir effetuer la détection multiple
        final String rawvalue = dataQR;
        String result;


        // On vérfie si la chaine est encodée en base64
        if (dataQR.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) {
            // Début de la décompression
            try {
                dataQR = DecompressionJSON.decompresser(dataQR);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(dataQR);
        // Si la chaine trouvée n'est pas compressée et qu'elle n'est pas du json, c'est alors simplement du texte
        // On rajoute le texte dans une chaine json comportement les attributs type et data
        // Le texte est alors traité comme un qrcode atomique
        if (!dataQR.startsWith("{\"name\"")) {
            dataQR = "{\"type\"=\"unique\",\"data\"=[\"" + dataQR + "\"]}";
        }

        Gson gson = new GsonBuilder().create();
        QrCodeJson code = gson.fromJson(dataQR, QrCodeJson.class);
        // Si le json est trop gros et est stocké sur le drive dans un fichier texte
        // Manque tests avec un qrcode réel

        if (code.getData().get(0).toString().startsWith("{type=file")){
            // LinkedTreeMap est la classe qui est instanciée lorsque GSON trouve du JSON
            if (code.getData().get(0) instanceof LinkedTreeMap) {
                FileJson file = QRCode.createJsonFile((LinkedTreeMap) code.getData().get(0));
                JSONDownloader downloader = new JSONDownloader(file.getUrl());
                downloader.execute();
                try {
                    result = downloader.get();
                    dataQR = DecompressionJSON.decompresser(result);
                    Gson gsonResult = new GsonBuilder().create();
                    code = gsonResult.fromJson(dataQR, QrCodeJson.class);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (code.getType().equalsIgnoreCase("atomique")||code.getType().equalsIgnoreCase("unique")||code.getType().equalsIgnoreCase("xl")) {
            return new QRCodeAtomique(code, rawvalue);
        } else if (code.getType().equalsIgnoreCase("ensemble")) {
            return new QRCodeEnsemble(code, rawvalue);
        } else
            return new QRCodeAtomique(code, rawvalue);


    }


}
