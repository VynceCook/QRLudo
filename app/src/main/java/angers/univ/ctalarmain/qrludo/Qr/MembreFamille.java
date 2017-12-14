package angers.univ.ctalarmain.qrludo.Qr;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by etudiant on 10/12/17.
 */

public class MembreFamille implements Comparable<MembreFamille>{
    private QrcodeAtomique element;
    private int rangDansLaFamille;

    public  MembreFamille(QrcodeAtomique elmt, int ordre){
        element=elmt;
        rangDansLaFamille = ordre;
    }

    public QrcodeAtomique getElement(){
        return element;
    }
    public int getRangDansLaFamille(){
        return rangDansLaFamille;
    }

    @Override
    public int compareTo(MembreFamille m){
        int compareRang=((MembreFamille)m).getRangDansLaFamille();
        return this.rangDansLaFamille-compareRang;
    }
}
