package fr.angers.univ.qrludo.QR.model;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeQuestionVocaleQCM extends QRCode{
    private ArrayList<Object> liste_rep = new ArrayList<>();
    private ArrayList<String> liste_bonne_reponse = new ArrayList<String>();
    private String id = null;
    private String text_bonne_reponse;
    private String m_text_mauvaise_rep;
    private boolean lettreReponseVocale;

    public QRCodeQuestionVocaleQCM(QrCodeJson code, String rawValue) {
        super(code, rawValue);

        Gson gson = new GsonBuilder().create();
        QRCodeJsonQuestionVocale codeQuestion = gson.fromJson(rawValue, QRCodeJsonQuestionVocale.class);

        liste_rep = codeQuestion.getData();
        id = codeQuestion.getId();
        text_bonne_reponse = codeQuestion.getText_bonne_reponse();
        m_text_mauvaise_rep = codeQuestion.getText_mauvaise_reponse();
        lettreReponseVocale = codeQuestion.getLettreReponseVocale();



        for(Object reponse : liste_rep){
            ArrayList rep = new ArrayList();
            rep = (ArrayList) reponse ;
            /* Si le second champs du tableau (représentant une réponse) est égal à true
             * on ajoute les autres champs à la liste des bonnes réponses
            */
            if(Boolean.parseBoolean(rep.get(1).toString()) ){
                // Si lettreReponseVocale = true , on prend le 1er champ sinon le 3e
                if(lettreReponseVocale){
                    liste_bonne_reponse.add(rep.get(0).toString().toLowerCase());
                }else{
                    liste_bonne_reponse.add(rep.get(2).toString().toLowerCase());
                }
            }

        }

        m_content.add(new QRText(m_qrcodeJson.getName()));
    }
    public String getQuestionText(){
        return m_qrcodeJson.getName();
    }

    public ArrayList<Object> getListe_rep(){ return liste_rep;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getM_text_bonne_rep() {
        return text_bonne_reponse;
    }

    public void setM_text_bonne_rep(String m_text_bonne_rep) {
        this.text_bonne_reponse = m_text_bonne_rep;
    }

    public boolean getLettreReponseVocale(){return lettreReponseVocale;}

    public String getM_text_mauvaise_rep() {
        return m_text_mauvaise_rep;
    }

    public void setM_text_mauvaise_rep(String m_text_mauvaise_rep) {
        this.m_text_mauvaise_rep = m_text_mauvaise_rep;
    }

    public ArrayList<String> getListe_bonne_reponse() {
        return liste_bonne_reponse;
    }
}
