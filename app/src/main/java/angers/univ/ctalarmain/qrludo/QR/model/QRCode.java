package angers.univ.ctalarmain.qrludo.QR.model;


import java.util.ArrayList;

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
        if(data.startsWith(("{type"))){
            return true;
        }
        else
            return false;

    }

}
