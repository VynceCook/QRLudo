package angers.univ.ctalarmain.qrludo.QR.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;

import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;


/**
 * Created by Florian Lherbeil
 */


/**
 * Represents the information of a QRCodeAtomique.
 */
public class QRCodeAtomique extends QRCode {

    /**
     *
     * @param code QrCode Json obtenu dans la classe QRCodeBuilder
     * @param rawValue valeur initiale contenue dans le qrcode
     * @throws UnhandledQRException
     */
    public QRCodeAtomique(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);
        FileJson music=new FileJson();
        for(Object data : code.getData()){
            System.out.println(data.getClass());
            System.out.println(data.toString());
            if(isUrlFile(data.toString())){
                if(data instanceof LinkedTreeMap){
                    LinkedTreeMap l = (LinkedTreeMap)data;
                    FileJson fj = new FileJson();
                    for(Object entry : l.entrySet()){
                        Map.Entry e = (Map.Entry)entry;
                        System.out.println("value : "+e.getValue().toString());
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
                    music = fj;
                }
                if(music.getType().equalsIgnoreCase("music")){
                    String url = music.getUrl();
                    m_content.add(new QRFile(url));
                }
            }
            else {
                m_content.add(new QRText(data.toString()));
            }
        }

    }

}
