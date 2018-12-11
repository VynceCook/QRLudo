package angers.univ.ctalarmain.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Valentine Rahier
 */
public class QRCodeReponse extends QRCode {

    private Long m_id;
    public QRCodeReponse(QrCodeJson code,String rawValue) {
        super(code, rawValue);

        JsonObject reponse = new Gson().fromJson(m_rawValue, JsonObject.class);

        m_id = reponse.get("id").getAsLong();

        m_content.add(new QRText(m_qrcodeJson.getName()));

    }

    public void setId(Long id){
        m_id = id;
    }

    public Long getId(){
        return m_id;
    }
}
