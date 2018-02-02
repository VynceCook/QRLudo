package angers.univ.ctalarmain.qrludo.QR.model;

/**
 * Created by etudiant on 20/01/18.
 */

public abstract class QRContent {

    protected String m_content;

    public QRContent(String content){
        m_content = content;
    }

    public String getContent(){
        return m_content;
    }

}
