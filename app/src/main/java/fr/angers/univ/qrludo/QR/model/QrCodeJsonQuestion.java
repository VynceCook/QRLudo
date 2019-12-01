package fr.angers.univ.qrludo.QR.model;

import java.util.ArrayList;

public class QrCodeJsonQuestion extends QrCodeJson {
    //private ArrayList<String> liste_bonne_rep = new ArrayList<>();
    private String text_bonne_reponse = null;
    private String text_mauvaise_reponse = null;
    private int nb_min_reponses = 0;

    @Override
    public String toString() {
        return super.toString()+" QrCodeJsonQuestion{" +
                ", id='" + super.getId() + '\'' +
                ", m_text_bonne_rep=" + text_bonne_reponse +
                ", m_text_mauvaise_rep='" + text_mauvaise_reponse + '\'' +
                '}';
    }

    public String getText_mauvaise_reponse() {
        return text_mauvaise_reponse;
    }

    public void setText_mauvaise_reponse(String text_mauvaise_reponse) {
        this.text_mauvaise_reponse = text_mauvaise_reponse;
    }

    public int getNb_min_reponses() {
        return nb_min_reponses;
    }

    public void setNb_min_reponses(int nb_min_reponses) {
        this.nb_min_reponses = nb_min_reponses;
    }

    public String getText_bonne_reponse() {
        return text_bonne_reponse;
    }

    public void setText_bonne_reponse(String text_bonne_reponse) {
        this.text_bonne_reponse = text_bonne_reponse;
    }

}
