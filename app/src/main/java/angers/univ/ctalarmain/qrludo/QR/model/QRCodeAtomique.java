package angers.univ.ctalarmain.qrludo.QR.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;
import angers.univ.ctalarmain.qrludo.utils.FileDowloader;

/**
 * Created by Jules Leguy on 20/01/18.
 *
 */


/**
 * Represents the information of a QRCodeAtomique. It can belong to a family or not.
 */
public class QRCodeAtomique extends QRCode {

    /**
     * Constructor that builds the object from a string.
     * The string is in Json
     *
     * @param code
     */
    public QRCodeAtomique(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        System.out.println(code);

        boolean contenuRead = false;

        for(Object data : code.getData()){
            if(isUrlFile(data.toString())){
                Gson gson = new GsonBuilder().create();
                final MusicJson music = gson.fromJson(data.toString(),MusicJson.class);
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
