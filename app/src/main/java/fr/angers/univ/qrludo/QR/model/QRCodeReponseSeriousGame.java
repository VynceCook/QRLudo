package fr.angers.univ.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class QRCodeReponseSeriousGame extends QRCode {

    private String reponse;
    private boolean isGoodAnswer;

    public QRCodeReponseSeriousGame(QrCodeJson code, String rawValue) {
        super(code, rawValue);
        Log.v("Réponse", "SeriousGame");
        Gson gson = new GsonBuilder().create();
        QRCodeJsonReponseSeriousGame codeReponse = gson.fromJson(rawValue, QRCodeJsonReponseSeriousGame.class);

        this.reponse = codeReponse.getReponse();
        this.isGoodAnswer = codeReponse.isGoodAnswer();

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public String getReponse() {
        return reponse;
    }

    public boolean isGoodAnswer() {
        return isGoodAnswer;
    }
}
