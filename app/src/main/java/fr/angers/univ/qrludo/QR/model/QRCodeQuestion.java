package fr.angers.univ.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;


/**
 * Valentine Rahier
 */
public class QRCodeQuestion extends QRCode {
    private ArrayList<Object> liste_bonne_rep = new ArrayList<>();
    private String id = null;
    private String m_text_bonne_rep;
    private String m_text_mauvaise_rep;
    private int nb_min_reponses;



    public QRCodeQuestion(QrCodeJson code, String rawValue) {
        super(code, rawValue);

        //id = m_qrcodeJson.

        Gson gson = new GsonBuilder().create();
        QrCodeJsonQuestion codeQuestion = gson.fromJson(rawValue, QrCodeJsonQuestion.class);

        liste_bonne_rep = codeQuestion.getData();
        id = codeQuestion.getId();
        m_text_bonne_rep = codeQuestion.getText_bonne_reponse();
        m_text_mauvaise_rep = codeQuestion.getText_mauvaise_reponse();
        nb_min_reponses = codeQuestion.getNb_min_reponses();


        m_content.add(new QRText(m_qrcodeJson.getName()));
    }

    public String getQuestionText(){
        return m_qrcodeJson.getName();
    }

    public ArrayList<Object> getListe_bonne_rep() {
        return liste_bonne_rep;
    }

    public void setListe_bonne_rep(ArrayList<Object> liste_bonne_rep) {
        this.liste_bonne_rep = liste_bonne_rep;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public int getNb_min_reponses() {
        return nb_min_reponses;
    }

    public void setNb_min_reponses(int nb_min_reponses) {
        this.nb_min_reponses = nb_min_reponses;
    }
}
