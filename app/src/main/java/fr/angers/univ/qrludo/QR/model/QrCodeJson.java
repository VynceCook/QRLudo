package fr.angers.univ.qrludo.QR.model;

/**
 * Created by Florian Lherbeil
 * Classe permettant de récupérer les informations du json contenu dans le qrcode
 */

import java.util.ArrayList;

public class QrCodeJson {
    private String name="";
    private String type="";



    // Ce tableau contient toute les données contenu dans la balise data
    // Ces données peuvent être sous la forme d'une chaine de caractère ou d'une nouvelle chaine de type Json
    private ArrayList<Object> data= new ArrayList<>();
    private String color="";

    private int version=-1;

    @Override
    public String toString() {
        return "QrCodeJson{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", data=" + data.size() +
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

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(ArrayList<Object> data) {
        this.data = data;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getVersion(){
        return this.version;
    }

    public void setVersion(int version){
        this.version = version;
    }
}

