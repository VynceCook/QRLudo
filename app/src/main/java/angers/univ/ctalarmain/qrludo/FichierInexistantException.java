package angers.univ.ctalarmain.qrludo;

/**
 * Created by etudiant on 27/11/17.
 */

public class FichierInexistantException extends Exception {

    public FichierInexistantException(){
        super();
    }

    @Override
    public String getMessage() {
        return "Le fichier n'existe pas sur le drive";
    }
}
