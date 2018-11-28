package angers.univ.ctalarmain.qrludo.QR.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;

/**
 * Created by Florian Lherbeil
 */

public class QRCodeEnsemble extends QRCode{

    /**
     *
     * @param code QrCode Json obtenu dans la cl
     * @param rawValue valeur initiale contenue dans le qrcode
     * @throws UnhandledQRException Exception levée si le qrcode ensemble n'est pas valide
     */
    public QRCodeEnsemble(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code, rawValue);
        FileJson music = new FileJson();
        for (Object data : code.getData()) {
            if (data instanceof LinkedTreeMap) {
                music = createJsonFile((LinkedTreeMap) data);
                if (music.getType().equalsIgnoreCase("music")) {
                    String url = music.getUrl();
                    System.out.println("url :"+url);
                    m_content.add(new QRFile(url));
                }
                //m_content.add(new QRFile(data.toString()));
            }
            /*else {
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }*/
        }
    }

    }

