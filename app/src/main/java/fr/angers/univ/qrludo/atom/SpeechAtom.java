package fr.angers.univ.qrludo.atom;

public class SpeechAtom extends Atom {
    private String content;

    public SpeechAtom(String content){
        this.content=content;
    }

    public String toString() {return "SpeechAtom "+ content;}
}
