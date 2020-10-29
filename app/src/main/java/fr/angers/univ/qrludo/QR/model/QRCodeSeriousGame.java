package fr.angers.univ.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class QRCodeSeriousGame extends QRCode {

    private String textToRead;
    private int nodeID;

    public QRCodeSeriousGame(QrCodeJson qr, String rawValue){
        super(qr,rawValue);
        Log.v("Qrcode", "SeriousGame");
        Gson gson = new GsonBuilder().create();
        QrCodeJsonSeriousGame code = gson.fromJson(rawValue, QrCodeJsonSeriousGame.class);

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public String getQuestionText(){
        return m_qrcodeJson.getName();
    }
}
