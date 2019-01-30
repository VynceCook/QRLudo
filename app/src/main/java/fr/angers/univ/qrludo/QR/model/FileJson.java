package fr.angers.univ.qrludo.QR.model;

/**
 * Created by Florian Lherbeil
 */

/**
 * Classe permettant de récupérer les informations sur les musiques contenues dans un json
 */
public class FileJson {
    private String type="";
    private Object url;
    private String name="";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url.toString();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
