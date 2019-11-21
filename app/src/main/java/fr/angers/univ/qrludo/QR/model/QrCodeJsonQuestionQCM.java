package fr.angers.univ.qrludo.QR.model;

public class QrCodeJsonQuestionQCM extends QrCodeJson{
    //private ArrayList<String> liste_bonne_rep = new ArrayList<>();
    private String id = null;
    private String text = null; //Affiche la question et les réponses
    private String text_bonne_reponse = null;
    private String text_mauvaise_reponse = null;

    @Override
    public String toString() {
        return super.toString()+" QrCodeJsonQuestion{" +
                ", id='" + id + '\'' +
                ", m_text_bonne_rep=" + text_bonne_reponse +
                ", m_text_mauvaise_rep='" + text_mauvaise_reponse + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText_mauvaise_reponse() {
        return text_mauvaise_reponse;
    }

    public void setText_mauvaise_reponse(String text_mauvaise_reponse) {
        this.text_mauvaise_reponse = text_mauvaise_reponse;
    }

    public String getText_bonne_reponse() {
        return text_bonne_reponse;
    }

    public void setText_bonne_reponse(String text_bonne_reponse) {
        this.text_bonne_reponse = text_bonne_reponse;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
