package fr.angers.univ.qrludo.QR.model;

/**
 * Created by Pierre-Yves Delépine
 */
public class QRCodeJsonQuestionVocale extends QrCodeJson {
    private String text_bonne_reponse = null;
    private String m_text_mauvaise_rep = null;
    private boolean lettreReponseVocale = false;

    @Override
    public String toString() {
        return super.toString()+"QRCodeJsonQuestionVocale{" +
                ", id='" + super.getId() + '\'' +
                ", lettreReponseVocale=" + lettreReponseVocale +
                ", text_bonne_reponse='" + text_bonne_reponse + '\'' +
                ", m_text_mauvaise_rep='" + m_text_mauvaise_rep + '\''+
                '}';
    }
    public String getText_bonne_reponse() {
        return text_bonne_reponse;
    }

    public void setText_bonne_reponse(String text_bonne_reponse) {
        this.text_bonne_reponse = text_bonne_reponse;
    }

    public boolean getLettreReponseVocale(){return this.lettreReponseVocale;}

    public String getM_text_mauvaise_rep() {
        return m_text_mauvaise_rep;
    }

    public void setM_text_mauvaise_rep(String m_text_mauvaise_rep) {
        this.m_text_mauvaise_rep = m_text_mauvaise_rep;
    }
}
