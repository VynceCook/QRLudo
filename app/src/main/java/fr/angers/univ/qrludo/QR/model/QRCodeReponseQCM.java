package fr.angers.univ.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QrCodeJson;

public class QRCodeReponseQCM extends QRCode {
    private String id = null;
    private boolean isAnswer;

    public QRCodeReponseQCM(QrCodeJson code, String dataQR) {
        super(code,dataQR);

        Gson gson = new GsonBuilder().create();
        QrCodeJsonReponseQCM codeReponse = gson.fromJson(dataQR, QrCodeJsonReponseQCM.class);
        id = codeReponse.getId();
        isAnswer = codeReponse.isAnswer();

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public boolean isAnswer() {
        return isAnswer;
    }

    public void setAnswer(boolean answer) {
        isAnswer = answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
