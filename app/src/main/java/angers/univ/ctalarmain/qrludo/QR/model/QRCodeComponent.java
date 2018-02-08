package angers.univ.ctalarmain.qrludo.QR.model;

import java.util.List;

/**
 * Created by Jules Leguy on 20/01/18.
 */

/**
 * Using the Composite design pattern to manage QR Families
 * The leafs are QRCodeAtomique or QRCodeEnsemble
 * The composites are only FamilleQRCodesComposite
 *
 * The leafs and the composites must return a sorted list of their content (QRContent object)
 *
 */
public interface QRCodeComponent {

    //Each component possesses a list of QRContent (url or text)
    List<QRContent> getQRContent();

    //Each component must be able to notify each contained QRFile to start downloading
    void downloadQRFiles();

    //Method that each QRCodeComponent must implement to notify if it contains the QRCode defined by the string
    boolean contains(String str);

}
