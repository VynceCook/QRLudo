package fr.angers.univ.qrludo.QR.model;

public class QrCodeJsonReponseQCM extends QrCodeJson {
    private boolean isAnswer;
    private String id = null;

    public boolean isAnswer() {
        return isAnswer;
    }

    public void setAnswer(boolean answer) {
        isAnswer = answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
