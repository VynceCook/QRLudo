package angers.univ.ctalarmain.qrludo.QR.model;


import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;

/**
 * Created by Florian Lherbeil
 */

public class QRCodeEnsemble extends QRCode{

    public QRCodeEnsemble(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        for(Object data : code.getData()){
            if(isUrlFile(data.toString())){
                m_content.add(new QRFile(data.toString()));
            }
            else {
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }
        }

    }

}
