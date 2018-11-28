package angers.univ.ctalarmain.qrludo.QR.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        super(code,rawValue);

        for(Object data : code.getData()){
            //if(isUrlFile(data.toString())) {
            /*Gson gson = new GsonBuilder().create();
            final FileJson music = gson.fromJson(data.toString(),FileJson.class);
            if(music.getType().equalsIgnoreCase("music")){
                String url = music.getUrl();
                m_content.add(new QRFile(url));
            }*/
                m_content.add(new QRFile(data.toString()));
           /* }
            else {
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }*/
        }

    }

}
