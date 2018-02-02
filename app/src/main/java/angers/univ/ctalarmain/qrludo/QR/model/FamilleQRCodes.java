package angers.univ.ctalarmain.qrludo.QR.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;

/**
 * Created by Jules Leguy on 20/01/18.
 */

/**
 * Stores a family of QRCodeAtomiques.
 * When a QRCodeAtomique is added to the family, it is inserted at the right place in the list.
 * The method getQRContent() returns each content of each QRCodeAtomique of the family in the right sequence.
 */
public class FamilleQRCodes implements QRCodeComponent {

    private String m_familyName;
    private TreeMap<Integer, QRCodeAtomique> m_sortedFamily;


    public FamilleQRCodes(String familyName){
        m_familyName = familyName;
        m_sortedFamily = new TreeMap<Integer, QRCodeAtomique>();
    }

    public String getFamilyName(){
        return m_familyName;
    }

    //Adds the given QRCodeAtomique to the family at the right place.
    //If the QRCodeAtomique doesn't belongs to a family or belongs to a different family, throw a FamilyException()
    public void addQRToFamily(QRCodeAtomique qr) throws FamilyException {

        if (qr.belongsToFamily() && qr.getFamilyName().equals(m_familyName)){
            Log.v("test", "ajout à famille");
            m_sortedFamily.put(qr.getFamilyRank(), qr);
        }
        else{
            throw new FamilyException();
        }

    }

    @Override
    public List<QRContent> getQRContent() {

        //Creating the list
        ArrayList<QRContent> out = new ArrayList<QRContent>();

        //Adding the content of each QRCodeAtomique of the family
        for(Map.Entry<Integer, QRCodeAtomique> entry : m_sortedFamily.entrySet()) {
            QRCodeAtomique currentQR = entry.getValue();
            out.addAll(currentQR.getQRContent());
        }

        return out;

    }

    @Override
    /**
     * Tell each contained QRFile to start downloading
     */
    public void downloadQRFiles() {
        for(Map.Entry<Integer, QRCodeAtomique> entry : m_sortedFamily.entrySet()) {
            entry.getValue().downloadQRFiles();
        }
    }

    //Returns true if the QRCode represented by the string belongs to the family
    @Override
    public boolean contains(String str) {

        for(Map.Entry<Integer, QRCodeAtomique> entry : m_sortedFamily.entrySet()) {
            QRCodeAtomique currentQR = entry.getValue();

            if (currentQR.contains(str)){
                return true;
            }
        }

        return false;
    }
}
