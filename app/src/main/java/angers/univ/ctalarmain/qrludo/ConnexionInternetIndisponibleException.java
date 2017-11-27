package angers.univ.ctalarmain.qrludo;

/**
 * Created by etudiant on 27/11/17.
 */

public class ConnexionInternetIndisponibleException extends Exception {

    public ConnexionInternetIndisponibleException(){
        super();
    }

    @Override
    public String getMessage() {
        return "Connexion à Internet indisponible";
    }
}
