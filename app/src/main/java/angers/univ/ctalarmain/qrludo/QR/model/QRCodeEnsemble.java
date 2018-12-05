package angers.univ.ctalarmain.qrludo.QR.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileInputStream;
import java.util.Map;

import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;

/**
 * Created by Florian Lherbeil
 */

public class QRCodeEnsemble extends QRCode{

    /**
     *
     * @param code QrCode Json
     * @param rawValue valeur initiale contenue dans le qrcode
     * @throws UnhandledQRException Exception levée si le qrcode ensemble n'est pas valide
     */
    public QRCodeEnsemble(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code, rawValue);
        FileJson music;
        for (Object data : code.getData()) {
            if (data instanceof LinkedTreeMap) {
                // Si on rencontre un QR Code complet dans les data, on récupère les lien qu'il contient
                if (data.toString().startsWith("{qrcode")) {
                    LinkedTreeMap linkedTreeMap = (LinkedTreeMap) data;
                    for (Object entry : linkedTreeMap.entrySet()) {
                        Map.Entry e = (Map.Entry) entry;
                        if (e.getKey().toString().equalsIgnoreCase("qrcode")) {
                            QrCodeJson tempcode = QRCode.createQRCode((LinkedTreeMap) e.getValue());
                            for (Object data2 : tempcode.getData()) {
                                if (isUrlFile(data2.toString())) {
                                    if (data2 instanceof LinkedTreeMap) {
                                        music = createJsonFile((LinkedTreeMap) data2);
                                        if (music.getType().equalsIgnoreCase("music")) {
                                            String url = music.getUrl();
                                            m_content.add(new QRFile(url));
                                            }
                                    }
                                }
                            }

                        }

                    }


                }
                else {
                    // Ancien code si jamais on décide de revenir au stockage des liens uniquement
                    music = createJsonFile((LinkedTreeMap) data);
                    if (music.getType().equalsIgnoreCase("music")) {
                        String url = music.getUrl();
                        System.out.println("url :"+url);
                        m_content.add(new QRFile(url));
                    }
                }
            } else {
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }
        }
    }
    }


