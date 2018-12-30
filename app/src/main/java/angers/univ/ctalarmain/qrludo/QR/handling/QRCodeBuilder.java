package angers.univ.ctalarmain.qrludo.QR.handling;

import android.util.Log;

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
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeQuestion;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeReponse;
import angers.univ.ctalarmain.qrludo.QR.model.QrCodeJson;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;
import angers.univ.ctalarmain.qrludo.exceptions.UnsupportedQRException;
import angers.univ.ctalarmain.qrludo.utils.DecompressionJSON;
import angers.univ.ctalarmain.qrludo.utils.JSONDownloader;


/**
 * Created by Jules Leguy on 20/01/18.
 * Modified by Florian Lherbeil
 * Builds a QRCode object from a Json object
 */
public class QRCodeBuilder {


    public static QRCode build(String dataQR, int current_version) throws UnhandledQRException, UnsupportedQRException {
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
        if (!dataQR.startsWith("{")) {
            dataQR = "{\"type\"=\"unique\",\"data\"=[\"" + dataQR + "\"]}";
        }

        Log.v("data_qr", dataQR);
        Gson gson = new GsonBuilder().create();
        QrCodeJson code = gson.fromJson(dataQR, QrCodeJson.class);

        if(code.getVersion()>current_version){
            throw new UnsupportedQRException("Ce QRCode ne peut pas être lu par cette application, veuillez mettre à jour QRLudo ou QRLudoGénérator");

        }
        // Si le json est trop gros et est stocké sur le drive dans un fichier texte
        // Manque tests avec un qrcode réel

            // LinkedTreeMap est la classe qui est instanciée lorsque GSON trouve du JSON
            if ((code.getData().size() > 0) && (code.getData().get(0) instanceof LinkedTreeMap)) {
                FileJson file = QRCode.createJsonFile((LinkedTreeMap) code.getData().get(0));
                if (file.getType().equalsIgnoreCase("json")) {
                    JSONDownloader downloader = new JSONDownloader(file.getUrl());
                    downloader.execute();
                    try {
                        result = downloader.get();
                        System.out.println("result : "+result);
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
        } else if(code.getType().equalsIgnoreCase("question")){
            Log.v("test_scan", code.getType());
            Log.v("test_scan", String.valueOf(code.getVersion()));
            Log.v("test_scan", code.getColor());
            Log.v("test_scan", code.getData().toString());
            return new QRCodeQuestion(code, dataQR);
        } else if(code.getType().equalsIgnoreCase("reponse")){
            return new QRCodeReponse(code, dataQR);
        }

        return new QRCodeAtomique(code, rawvalue);

    }


}
