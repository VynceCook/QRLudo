package fr.angers.univ.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class QRCodeSeriousGame extends QRCode {

    private String textToRead;
    private int nodeID;

    public QRCodeSeriousGame(QrCodeJson qr, String rawValue){
        super(qr,rawValue);

        Gson gson = new GsonBuilder().create();
        QrCodeJsonSeriousGame code = gson.fromJson(rawValue, QrCodeJsonSeriousGame.class);

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }
}
