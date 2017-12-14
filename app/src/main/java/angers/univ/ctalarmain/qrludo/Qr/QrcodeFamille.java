package angers.univ.ctalarmain.qrludo.Qr;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by etudiant on 09/12/17.
 */

public class QrcodeFamille{

    private String nomDeLaFamille;
    //private ArrayList<MembreFamille> famille;
    //private ArrayList<QrcodeAtomique> famille;
    private TreeMap<Integer, QrcodeAtomique> m_famille;

    public QrcodeFamille(String nom){
        nomDeLaFamille=nom;
        //famille=new ArrayList<MembreFamille>();
        //this.famille = new ArrayList<QrcodeAtomique>();
        m_famille = new TreeMap<Integer, QrcodeAtomique>();
    }
    public String getNomDeLaFamille(){
        return nomDeLaFamille;
    }

    public void addMembreFamille(QrcodeAtomique qr, int rang){
        //famille.add(new MembreFamille(qr,rang));
        qr.setRang(rang);

        Log.v("element", "rang actuel "+rang);

        m_famille.put(rang, qr);

        Log.v("element","on a ajouté un nouvel élément dans la famille; nouvelle taille : "+m_famille.size());

    }

    //public ArrayList<MembreFamille> getFamille(){
      //  return famille;
    //}
/*
    public ArrayList<MembreFamille> getFamille() {
            return famille;
    }
    */

    /**
     * Renvoie une ArrayList de tous les QRCodes de la famille triés selon leur rang dans la famille
     *
     * @return
     */
    public ArrayList<QrcodeAtomique> getFamille(){

        ArrayList<QrcodeAtomique> out = new ArrayList<QrcodeAtomique>();

        for(Map.Entry<Integer, QrcodeAtomique> entry : m_famille.entrySet()) {
            Integer key = entry.getKey();
            QrcodeAtomique qr = entry.getValue();
            out.add(qr);
        }

        return out;
    }

    public boolean existeQr(String qrCOntenu){

        ArrayList<QrcodeAtomique> familleListe = this.getFamille();

        for (QrcodeAtomique qr : familleListe){
            if (qr.getDataQrcode()==qrCOntenu){
                return true;
            }
        }

        return false;

    }
}

