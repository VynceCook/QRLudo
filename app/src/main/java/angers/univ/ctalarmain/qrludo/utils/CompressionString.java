package angers.univ.ctalarmain.qrludo.utils;

import com.google.api.client.util.Base64;

/**
 * Created by Florian Lherbeil
 * Classe servant à compresser une chaine de caractère donner un nom reconnaissable aux fichers sans
 * passer par l'id qui est propre à google drive
 * Cette chaine n'aura jamais besoin d'être décompressée
 */
public class CompressionString {
    public static String compress(String str) {
        //On réduit la taille de la chaine si possible
        str= str.replace("https://","");
        byte[] encodedBytes = Base64.encodeBase64(str.getBytes());
        return new String(encodedBytes).replace("=","");
    }
}
