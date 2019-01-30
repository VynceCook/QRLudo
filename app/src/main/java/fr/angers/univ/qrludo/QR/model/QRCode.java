package fr.angers.univ.qrludo.QR.model;


import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jules Leguy on 20/01/18.
 * Modified by Florian Lherbeil
 */

public abstract class QRCode implements QRCodeComponent {

    //List of the content (links or text)
    protected ArrayList<QRContent> m_content;

    //String detected on the QRCode. Kept to compare easily if two QRCodes are contains
    protected String m_rawValue;

    protected QrCodeJson m_qrcodeJson;

    public QRCode(QrCodeJson code, String rawValue){
        m_qrcodeJson = code;
        m_rawValue = rawValue;
        m_content = new ArrayList<QRContent>();
    }


    public boolean contains(String str){
        return str.equals(m_rawValue);
    }

    public String getRawValue(){
        return m_rawValue;
    }

    public ArrayList<QRContent> getQRContent(){
        return m_content;
    }


    /**
     * Tell each contained QRFile to start downloading
     */
    @Override
    public void downloadQRFiles() {

        for (QRContent qrContent : m_content){

            if (qrContent instanceof QRFile){
                ((QRFile) qrContent).downloadIfNotInMemory();
            }

        }
    }
    public boolean isUrlFile(String data){
        return data.contains(("url="));
    }

    /**
     *
     * @param linkedTreeMap
     * @return une instance de FileJson
     *
     * Cette fonction permet de récupérer les valeurs contenues dans les data sous forme json
     * comme pour les musiques ou les fichiers stockés sur le drive sans passer par gson
     *
     */
    public static FileJson createJsonFile(LinkedTreeMap linkedTreeMap){
        FileJson fj = new FileJson();
        for(Object entry : linkedTreeMap.entrySet()){
            Map.Entry e = (Map.Entry)entry;
            if(e.getKey().toString().equalsIgnoreCase("type")){
                fj.setType(e.getValue().toString());
            }
            else if(e.getKey().toString().equalsIgnoreCase("name")){
                fj.setName(e.getValue().toString());
            }
            else if(e.getKey().toString().equalsIgnoreCase("url")){
                fj.setUrl(e.getValue().toString());
            }
        }
        return fj;
    }

    /**
     * Cette fonction sert dans la récupération des liens du QR Code Ensemble
     * On ne peut pas utiliser Gson car il ne peut gérer le lien que le json contient
     * @param linkedTreeMap
     * @return
     */
    public static QrCodeJson createQRCode(LinkedTreeMap linkedTreeMap){
        QrCodeJson qr  = new QrCodeJson();
        for(Object entry : linkedTreeMap.entrySet()){
            Map.Entry e = (Map.Entry)entry;
            if(e.getKey().toString().equalsIgnoreCase("color")){
                qr.setColor(e.getValue().toString());
            }
            else if(e.getKey().toString().equalsIgnoreCase("name")){
                qr.setName(e.getValue().toString());
            }
            else if(e.getKey().toString().equalsIgnoreCase("type")){
                qr.setType(e.getValue().toString());
            }
            else if(e.getKey().toString().equalsIgnoreCase("data")){
                qr.setData((ArrayList)e.getValue());
            }
        }
        return qr;
    }

}
