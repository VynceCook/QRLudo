package fr.angers.univ.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.File;

import fr.angers.univ.qrludo.exceptions.UnhandledQRException;
import fr.angers.univ.qrludo.utils.FileDownloader;


/**
 * Created by Florian Lherbeil
 */


/**
 * Represents the information of a QRCodeAtomique.
 */
public class QRCodeAtomique extends QRCode {

    /**
     *
     * @param code QrCode Json obtenu dans la classe QRCodeBuilder
     * @param rawValue valeur initiale contenue dans le qrcode
     * @throws UnhandledQRException
     */

    private String m_reponse;
    private String m_id;
    private boolean m_isWebsite = false;


    public QRCodeAtomique(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        Log.i("test","Data dans unique : "+code.getData());

        Gson gson = new GsonBuilder().create();
        QrCodeJson codeQuestion = gson.fromJson(rawValue, QrCodeJsonQuestion.class);

        m_reponse = codeQuestion.getData().toString();
        m_id = code.getId();

        FileJson music=new FileJson();
        for(Object data : code.getData()){
            if(isUrlFile(data.toString())) {
                if (data instanceof LinkedTreeMap) {
                    music = createJsonFile((LinkedTreeMap) data);
                    if (music.getType().equalsIgnoreCase("music")) {
                        String url = music.getUrl();
                        m_content.add(new QRFile(url));
                    }
                }
            }
            else {
                // On construit le répertoire "qrludo" si ce dernier n'existe pas
                File targetDir = new File(FileDownloader.DOWNLOAD_PATH);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                m_content.add(new QRText(data.toString()));
            }
        }

    }

    public String getM_reponse() {
        return this.m_reponse;
    }

    public String getM_id() {
        return this.m_id;
    }

    public void setWebSite() { this.m_isWebsite = true; }

    public boolean isWebsite() { return m_isWebsite; }
}
