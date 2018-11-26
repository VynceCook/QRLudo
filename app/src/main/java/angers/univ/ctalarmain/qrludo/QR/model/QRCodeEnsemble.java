package angers.univ.ctalarmain.qrludo.QR.model;


import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;

/**
 * Created by Florian Lherbeil
 */

public class QRCodeEnsemble extends QRCode{

    /**
     *
     * @param code QrCode Json obtenu dans la cl
     * @param rawValue valeur initiale contenue dans le qrcode
     * @throws UnhandledQRException Exception levée si le qrcode ensemble n'est pas valide
     */
    public QRCodeEnsemble(QrCodeJson code,String rawValue) throws UnhandledQRException {
        super(code,rawValue);

        for(Object data : code.getData()){
            if(isUrlFile(data.toString())) {
                m_content.add(new QRFile(data.toString()));
            }
            else {
                throw new UnhandledQRException("QRCodeEnsemble cannot contain text");
            }
        }

    }

}
