package angers.univ.ctalarmain.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;


/**
 * Created by Florian Lherbeil
 */


/**
 * Represents the information of a QRCodeAtomique.
 */
public class QRCodeAtomique extends QRCode {

    /**
     *
     * @param code QrCode Json obtenu dans la cl
     * @param rawValue valeur initiale contenue dans le qrcode
     * @throws UnhandledQRException
     */
    public QRCodeAtomique(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        for(Object data : code.getData()){
            if(isUrlFile(data.toString())){
                Gson gson = new GsonBuilder().create();
                final FileJson music = gson.fromJson(data.toString(),FileJson.class);
                if(music.getType().equalsIgnoreCase("music")){
                    String url = music.getUrl();
                    m_content.add(new QRFile(url));
                }
            }
            else {
                m_content.add(new QRText(data.toString()));
            }
        }

    }

}
