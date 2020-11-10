package fr.angers.univ.qrludo.atom;

public class QRAtom extends Atom {
    private String content;

    public QRAtom(String content){
        this.content=content;
    }

    public String toString() {return "QRAtom "+content;}

    public String getContent() {return content;}
}
