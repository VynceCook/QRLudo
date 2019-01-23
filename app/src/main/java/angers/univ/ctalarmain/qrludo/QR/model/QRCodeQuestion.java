package angers.univ.ctalarmain.qrludo.QR.model;

import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;
import java.util.Map;


/**
 * Valentine Rahier
 */
public class QRCodeQuestion extends QRCode {
    private Map<String, String> m_reponses;

    public QRCodeQuestion(QrCodeJson code, String rawValue) {
        super(code, rawValue);


        m_reponses = new HashMap<>();

        for(Object reponse : m_qrcodeJson.getData()){
            LinkedTreeMap<String, Object> reponseString = (LinkedTreeMap<String, Object>)reponse;

            String id = (String) reponseString.get("id");
            String message = (String)reponseString.get("message");

            m_reponses.put(id, message);
        }

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public String getQuestionText(){
        return m_qrcodeJson.getName();
    }

    public Map<String, String> getReponses(){
        return m_reponses;
    }
}
