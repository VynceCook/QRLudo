package fr.angers.univ.qrludo.QR.model;

/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeJsonQuestionVocale extends QrCodeJson {
    private String text_bonne_reponse = null;
    private String text_mauvaise_reponse = "Dommage mauvaise réponse";

    private boolean lettreReponseVocale = false;

    @Override
    public String toString() {
        return super.toString()+"QRCodeJsonQuestionVocale{" +
                ", id='" + super.getId() + '\'' +
                ", lettreReponseVocale=" + lettreReponseVocale +
                ", text_bonne_reponse='" + text_bonne_reponse + '\'' +
                ", m_text_mauvaise_rep='" + text_mauvaise_reponse + '\''+
                '}';
    }
    public String getText_bonne_reponse() {
        return text_bonne_reponse;
    }

    public void setText_bonne_reponse(String text_bonne_reponse) {
        this.text_bonne_reponse = text_bonne_reponse;
    }

    public boolean getLettreReponseVocale(){return this.lettreReponseVocale;}

    public String getText_mauvaise_reponse() {
        return text_mauvaise_reponse;
    }

    public void setText_mauvaise_reponse(String text_mauvaise_reponse) {
        this.text_mauvaise_reponse = text_mauvaise_reponse;
    }
}
