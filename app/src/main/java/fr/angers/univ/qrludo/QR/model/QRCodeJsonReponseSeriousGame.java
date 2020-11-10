package fr.angers.univ.qrludo.QR.model;

public class QRCodeJsonReponseSeriousGame extends QrCodeJson {

    private String reponse;
    private boolean isGoodAnswer;

    public String toString(){
        return super.toString()+" QrCodeJsonReponseSeriousGame{" +
                ", id='" + super.getId() + "\'" +
                ", reponse='" + reponse + "\'" +
                ", isGoodAnswer=" + isGoodAnswer +
                "}";
    }

    public String getReponse() {
        return reponse;
    }

    public boolean isGoodAnswer(){
        return isGoodAnswer;
    }
}
