package angers.univ.ctalarmain.qrludo;

/**
 * Created by etudiant on 27/11/17.
 */

public class FichierDejaExistantException extends Exception {

    public FichierDejaExistantException(){
        super();
    }

    @Override
    public String getMessage() {
        return "Le fichier a déjà été téléchargé";
    }
}
