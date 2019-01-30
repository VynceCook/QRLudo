package fr.angers.univ.qrludo.exceptions;

/**
 * Created by Jules Leguy on 20/01/18.
 */

/**
 * Thrown when a QRCode object cannot be built from a XML object
 */
public class UnhandledQRException extends Exception {

    public UnhandledQRException(String message){
        super(message);
    }
}
