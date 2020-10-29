package fr.angers.univ.qrludo.QR.model;

import java.util.ArrayList;
/**
 * Created by Pierre-Yves Delépine
 */

public class QRCodeJsonScenarioLoader extends QrCodeJson{
    //Texte d'introduction
    private String introduction = null;
    // Texte de fin
    private String fin = null;
    // Contient un tableau d'égnimes représentées sous la forme
    // [id énigme,nom énigme,type énigme]
    private ArrayList<Object> enigmes = new ArrayList<Object>();
    /*
     * Contient un tableau des questions de type RecoVocale sous la forme
     * [id énigme associé à la question, texte de la question, réponse de la question]
     */
    private ArrayList<Object> questionsRecoVocale = new ArrayList<Object>();
    /* Contient un tableau des questions de type QrCode sous la forme
     * [
     *  id énigme associé à la question,
     *  texte de la question ,
     *  [[texte de la réponse],[boolean pour savoir si c'est la bonne réponse]]
     * ]
     */
    private ArrayList<Object> questionsQrCode = new ArrayList<Object>();

    @Override
    public String toString() {
        return "QRCodeJsonScenario{" +
                "textIntroduction='" + introduction + '\'' +
                ", textFin='" + fin + '\'' +
                ", enigmes=" + enigmes +
                ", questionsRecoVocale=" + questionsRecoVocale +
                ", questionsQrCode=" + questionsQrCode +
                '}';
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getFin() {
        return fin;
    }

    public ArrayList<Object> getEnigmes() {
        return enigmes;
    }

    public ArrayList<Object> getQuestionsRecoVocale() {
        return questionsRecoVocale;
    }

    public ArrayList<Object> getQuestionsQrCode() {
        return questionsQrCode;
    }
}
