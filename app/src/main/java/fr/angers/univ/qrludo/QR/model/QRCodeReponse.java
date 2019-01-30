package fr.angers.univ.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Valentine Rahier
 */
public class QRCodeReponse extends QRCode {

    private String m_id;
    public QRCodeReponse(QrCodeJson code,String rawValue) {
        super(code, rawValue);

        JsonObject reponse = new Gson().fromJson(m_rawValue, JsonObject.class);

        m_id = reponse.get("id").getAsString();

        m_content.add(new QRText(m_qrcodeJson.getName()));

    }

    public String getReponseText(){
        return m_qrcodeJson.getName();
    }

    public void setId(String id){
        m_id = id;
    }

    public String getId(){
        return m_id;
    }
}
