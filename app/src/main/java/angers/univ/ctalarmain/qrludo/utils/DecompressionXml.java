package angers.univ.ctalarmain.qrludo.utils;

/**
 * Created by Jules Leguy
 */

public class DecompressionXml {

    public static String decompresser(String donneesCompressees) {

        String out = "";

        //On vérifie que la chaîne est composée d'au moins 3 caractères et qu'elle commence par "Ѐ1" et donc qu'on sait la décompresser
        if (donneesCompressees.length()>2 && donneesCompressees.charAt(0)==1024 && donneesCompressees.charAt(1)==49) {

            //On itère sur tous les caractères de la chaîne compressée
            for (int i=2; i<donneesCompressees.length(); i++) {
                int utf8Value = donneesCompressees.charAt(i);

                //Cas où il s'agit d'un caractère non compressé (données rentrées par les transcripteurs)
                //On fait le test pour s'épargner le switch quand ce n'est pas nécessaire pour gagner du temps sur la lecture
                if (utf8Value<1024) {
                    out+=(char) utf8Value; //On ajoute le texte à la chaîne de sortie sans y toucher
                }
                else {

                    //Sinon on teste toutes les valeurs possibles et on ajoute la chaîne déduite en conséquence
                    switch(utf8Value) {

                        case 1025:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"atomique\"><contenu><texte>";
                            break;
                        case 1026:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"atomique\"><contenu><fichier url=\"";
                            break;
                        case 1027:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"atomique\"><contenu>";
                            break;
                        case 1028:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"ensemble\"><contenu><texte>";
                            break;
                        case 1029:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"ensemble\"><contenu><fichier url=\"";
                            break;
                        case 1030:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"ensemble\"><contenu>";
                            break;
                        case 1031:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"atomique\">";
                            break;
                        case 1032:
                            out+="<donneesutilisateur xmlns=\"http://www.w3.org/1999/xhtml\" type=\"ensemble\">";
                            break;
                        case 1033:
                            out+="<contenu><texte>";
                            break;
                        case 1034:
                            out+="<contenu><fichier url=\"";
                            break;
                        case 1035:
                            out+="</texte><texte>";
                            break;
                        case 1036:
                            out+="</texte><fichier url=\"";
                            break;
                        case 1037:
                            out+="</texte></contenu><famille nom=\"";
                            break;
                        case 1038:
                            out+="</texte></contenu></donneesutilisateur>";
                            break;
                        case 1039:
                            out+="</fichier><texte>";
                            break;
                        case 1040:
                            out+="</fichier><fichier url=\"";
                            break;
                        case 1041:
                            out+="</fichier></contenu><famille nom=\"";
                            break;
                        case 1042:
                            out+="</fichier></contenu></donneesutilisateur>";
                            break;
                        case 1043:
                            out+="</contenu><famille nom=\"";
                            break;
                        case 1044:
                            out+="</contenu></donneesutilisateur>";
                            break;
                        case 1045:
                            out+="\"></famille></donneesutilisateur>";
                            break;
                        case 1046:
                            out+="\"></famille>";
                            break;
                        case 1047:
                            out+="</donneesutilisateur>";
                            break;
                        case 1048:
                            out+="<fichier url=\"";
                            break;
                        case 1049:
                            out+="<famille nom=\"";
                            break;
                        case 1050:
                            out+="<texte></texte>";
                            break;
                        case 1051:
                            out+="<texte>";
                            break;
                        case 1052:
                            out+="</texte>";
                            break;
                        case 1053:
                            out+="\" ordre=\"";
                            break;
                        case 1054:
                            out+="\"></";
                            break;

                    }
                }
            }
        }
        else {
            //Si on ne sait pas décompresser la chaîne, on renvoie la chaîne initiale
            return donneesCompressees;
        }

        return out;
    }

}
