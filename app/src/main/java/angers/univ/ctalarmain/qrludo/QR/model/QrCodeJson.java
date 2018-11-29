package angers.univ.ctalarmain.qrludo.QR.model;

/**
 * Created by Florian Lherbeil
 * Classe permettant de récupérer les informations du json contenu dans le qrcode
 */

import java.util.ArrayList;

public class QrCodeJson {
    private String name="";
    private String type="";

    // Ce tableau contient toute les données contenu dans la balise data
    // Ces données peuvent être sous la forme d'une chaine de caractères ou d'une nouvelle chaine de type Json

    private ArrayList<Object> data= new ArrayList<>();
    private String color="";

    @Override
    public String toString() {
        return "QrCodeJson{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", data=" + data +
                ", color='" + color + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Object> getData() {
        return data;
    }

    public String getColor() {
        return color;
    }
}

