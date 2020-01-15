package fr.angers.univ.qrludo.QR.model;

public class QrCodeJsonReponseQCM extends QrCodeJson {
    private boolean isAnswer;

    public boolean isAnswer() {
        return isAnswer;
    }

    public void setAnswer(boolean answer) {
        isAnswer = answer;
    }
}
