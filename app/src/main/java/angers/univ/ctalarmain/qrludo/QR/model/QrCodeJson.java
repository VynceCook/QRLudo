package angers.univ.ctalarmain.qrludo.QR.model;

/**
 * Created by Florian Lherbeil
 */

import java.util.ArrayList;

public class QrCodeJson {
    private String name;
    private String type;
    private ArrayList<String> data;
    private String color;

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

    public ArrayList<String> getData() {
        return data;
    }

    public String getColor() {
        return color;
    }
}

