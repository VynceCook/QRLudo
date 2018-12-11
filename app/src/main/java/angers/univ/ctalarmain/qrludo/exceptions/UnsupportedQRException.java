package angers.univ.ctalarmain.qrludo.exceptions;

/**
 * Valentine Rahier
 * Classe permettant de vérifier la version du QRCode scanné
 */
public class UnsupportedQRException extends Exception {
    public UnsupportedQRException(String message){
        super(message);
    }
}
