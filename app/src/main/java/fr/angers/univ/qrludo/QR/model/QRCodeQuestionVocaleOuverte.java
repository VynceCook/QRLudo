package fr.angers.univ.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeQuestionVocaleOuverte extends QRCode{
    private String bonneReponse;
    private String id = null;
    private String m_text_bonne_rep;
    private String m_text_mauvaise_rep;

    public QRCodeQuestionVocaleOuverte(QrCodeJson code, String rawValue) {
        super(code, rawValue);

        Gson gson = new GsonBuilder().create();
        QRCodeJsonQuestionVocale codeQuestion = gson.fromJson(rawValue, QRCodeJsonQuestionVocale.class);

        bonneReponse = codeQuestion.getData().get(0).toString().toLowerCase();
        id = codeQuestion.getId();
        m_text_bonne_rep = codeQuestion.getText_bonne_reponse();
        m_text_mauvaise_rep = codeQuestion.getText_mauvaise_reponse();

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
        return m_text_bonne_rep;
    }


    public String getM_text_mauvaise_rep() {
        return m_text_mauvaise_rep;
    }

}
