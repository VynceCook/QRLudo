package fr.angers.univ.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeQuestionVocaleOuverte extends QRCode{
    private String bonneReponse;
    private String id = null;
    private String text_bonne_reponse;
    private String text_mauvaise_rep;

    public QRCodeQuestionVocaleOuverte(QrCodeJson code, String rawValue) {
        super(code, rawValue);

        Gson gson = new GsonBuilder().create();
        QRCodeJsonQuestionVocale codeQuestion = gson.fromJson(rawValue, QRCodeJsonQuestionVocale.class);

        bonneReponse = codeQuestion.getData().get(0).toString().toLowerCase();
        id = codeQuestion.getId();
        text_bonne_reponse = codeQuestion.getText_bonne_reponse();
        text_mauvaise_rep = codeQuestion.getText_mauvaise_reponse();

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public String getQuestionText(){
        return m_qrcodeJson.getName();
    }

    public String getBonneReponse(){return bonneReponse;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText_bonne_rep() {
        return text_bonne_reponse;
    }


    public String getText_mauvaise_rep() {
        return text_mauvaise_rep;
    }

}
