package angers.univ.ctalarmain.qrludo.Qr;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.channels.FileLockInterruptionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Created by etudiant on 16/11/17.
 */

public class Qrcode {
    private String dataQrcode;
    private String typeQrcode;
    /**
     * le fichier contenu dans le qrcode
     */
    private Fichier fichier;

    public Qrcode(String xmlData) {
        dataQrcode = xmlData;
    }

    public void setDataQrcode(String xmlData) {
        dataQrcode = xmlData;
    }

    public String getDataQrcode() {
        return dataQrcode;
    }

    public void setTypeQrcode(String type){
        typeQrcode = type;
    }

    public String getTypeQrcode(){
        return typeQrcode;
    }

    public Fichier getFichier(){
        return fichier;
    }

    public void setFichier(Fichier fichier) {
        this.fichier = fichier;
    }


    public class Fichier{
        private String name;
        private String id;

        public Fichier(String name, String id){
            this.name=name;
            this.id = id;
        }

        public String getNom(){
            return name;
        }

        public String getId(){
            return id;
        }

        public void setName(String name){
            this.name = name;
        }

        public void setUrl(String id){
            this.id = id;
        }
    }

}