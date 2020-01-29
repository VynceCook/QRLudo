package fr.angers.univ.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import fr.angers.univ.qrludo.QR.model.QRCode;
import fr.angers.univ.qrludo.QR.model.QrCodeJson;

public class QRCodeQuestionQCM extends QRCode {
    private String id = null;
    private String text;
    private String m_text_bonne_rep;
    private String m_text_mauvaise_rep;
    private int m_nombreReponses;


    public QRCodeQuestionQCM(QrCodeJson code, String dataQR) {
        super(code,dataQR);

        Gson gson = new GsonBuilder().create();
        QrCodeJsonQuestionQCM codeQuestion = gson.fromJson(dataQR, QrCodeJsonQuestionQCM.class);


        id = codeQuestion.getId();
        m_text_bonne_rep = codeQuestion.getText_bonne_reponse();
        m_text_mauvaise_rep = codeQuestion.getText_mauvaise_reponse();
        text = codeQuestion.getText();

        String textNombreReponsse = codeQuestion.getText_nombre_reponse();
        m_nombreReponses = Integer.parseInt(textNombreReponsse);

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public String getQuestionText(){
        return m_qrcodeJson.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNombreReponses() { return m_nombreReponses; }

    public String getM_text_bonne_rep() {
        return m_text_bonne_rep;
    }

    public void setM_text_bonne_rep(String m_text_bonne_rep) {
        this.m_text_bonne_rep = m_text_bonne_rep;
    }

    public String getM_text_mauvaise_rep() {
        return m_text_mauvaise_rep;
    }

    public void setM_text_mauvaise_rep(String m_text_mauvaise_rep) {
        this.m_text_mauvaise_rep = m_text_mauvaise_rep;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
