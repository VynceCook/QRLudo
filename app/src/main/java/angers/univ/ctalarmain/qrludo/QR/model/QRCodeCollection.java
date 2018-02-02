package angers.univ.ctalarmain.qrludo.QR.model;

/**
 * Created by Jules Leguy on 20/01/18.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import angers.univ.ctalarmain.qrludo.exceptions.FamilyException;

/**
 * Class whose role is to manage collections of QRCodes before they are read by the text to speech engine
 *
 */
public class QRCodeCollection{

    //Contains QRCodeComponents, which are QRCodeAtomique, QRCodeEnsemble of FamilleQRCodes
    private LinkedList<QRCodeComponent> m_QRList;

    //Contains the QRCodeComponents which have been detected but ignored (so that they can't be read anymore)
    private LinkedList<QRCodeComponent> m_QRIgnoredList;

    public QRCodeCollection(){
        m_QRList = new LinkedList<>();
        m_QRIgnoredList = new LinkedList<>();
    }

    /**
     * adds a QRCode at the end of the list if it isn't already a member
     * if it belongs to a family, adding it into the corresponding FamilleQRCodes if exists or creating it
     * @param qr
     */
    public void addQR(QRCode qr) {

        Log.v("test", "appel à addQR");

        if (!isAlreadyInCollection(qr.getRawValue())){
            Log.v("test", "appel à addQR");

            //If it is a QRCodeAtomique, checking if it belongs to a family
            if (qr instanceof QRCodeAtomique){

                Log.v("test", "le qrcode est atomique");

                if (((QRCodeAtomique) qr).belongsToFamily()){

                    try {
                        boolean existingFamily = false;

                        for (QRCodeComponent alreadyPresentQRComponent : m_QRList) {


                            //Looking for a QRCodeFamily for this QR in m_QRList
                            if (alreadyPresentQRComponent instanceof FamilleQRCodes && ((FamilleQRCodes) alreadyPresentQRComponent).getFamilyName().equals(((QRCodeAtomique) qr).getFamilyName())) {
                                //adding the qr code to the already existing FamilleQRCodes
                                ((FamilleQRCodes) alreadyPresentQRComponent).addQRToFamily((QRCodeAtomique) qr);
                                existingFamily = true;
                            }
                        }

                        //Creating the family if non-existing
                        if (!existingFamily) {
                            FamilleQRCodes newFamily = new FamilleQRCodes(((QRCodeAtomique) qr).getFamilyName());
                            newFamily.addQRToFamily((QRCodeAtomique) qr);
                            m_QRList.addLast(newFamily);
                        }
                    } catch (FamilyException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    //The QRCodeAtomique doesn't belong to a family : adding it directly to the list
                    m_QRList.addLast(qr);
                }
            }
            else{
                //It is not a QRCodeAtomique : adding it directly to the list
                m_QRList.addLast(qr);
            }
        }
        else{
        }
    }

    public void addIgnoredQR(QRCode qr){
        if (!isAlreadyIgnored(qr.getRawValue())){
            m_QRIgnoredList.add(qr);
        }
    }

    /**
     * Returns the number of QRCodeComponents which have been detected and not ignored
     * @return
     */
    public int getNbQR(){
        return m_QRList.size();
    }


    /**
     * returns a list of the content of the first QRCodeComponent of the list.
     * @return
     */
    public List<QRContent> getContentFirstQR(){
        if (m_QRList.size()!=0)
            return m_QRList.get(0).getQRContent();
        else
            return new ArrayList<QRContent>();
    }


    /**
     * returns the list of all the content of all the QRCodeComponent of the list, except the first one
     * @return
     */
    public List<QRContent> getContentAllQRButFirst(){

        ArrayList<QRContent> out = new ArrayList<QRContent>();

        for (int i=1; i<m_QRList.size(); i++) {
            out.addAll(m_QRList.get(i).getQRContent());
        }

        return out;
    }


    /**
     * returns the list of all the content of all the QRCodeComponent of the list
     * @return
     */
    public List<QRContent> getContentAllQR(){
        ArrayList<QRContent> out = new ArrayList<QRContent>();

        for (QRCodeComponent qrComponent : m_QRList){
            out.addAll(qrComponent.getQRContent());
        }

        return out;
    }

    /**
     * Removes all the detected QRCodeComponents, including the ignored ones
     */
    public void clear(){
        m_QRList.clear();
        m_QRIgnoredList.clear();
    }

    /**
     * Removes the ignored QRCodeComponents
     */
    public void clearIgnoredQRComponents(){
        m_QRIgnoredList.clear();
    }


    /**
     * returns true if the QRCode is already in the collection (as a QRCode or inside a FamilleQRCode)
     * @param qrRawValue
     * @return
     */
    public boolean isAlreadyInCollection(String qrRawValue){

        for (QRCodeComponent qrCodeComponent : m_QRList){
            if (qrCodeComponent.contains(qrRawValue)){
                return true;
            }
        }

        return false;
    }

    /**
     * returns true if the QRCode is already ignored
     * @param qrRawValue
     * @return
     */
    public boolean isAlreadyIgnored(String qrRawValue){

        for (QRCodeComponent qrCodeComponent : m_QRIgnoredList){
            if (qrCodeComponent.contains(qrRawValue)){
                return true;
            }
        }

        return false;
    }

    /**
     * Starts the downloading of each contained QRFile
     * Called when the internet connection was off when the QRCodes have been detected and the connection has just been opened
     */
    public void downloadAllFiles(){
        for (QRCodeComponent qrCodeComponent : m_QRList){
            qrCodeComponent.downloadQRFiles();
        }
    }
}
