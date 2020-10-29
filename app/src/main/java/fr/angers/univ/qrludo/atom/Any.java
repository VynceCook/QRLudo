package fr.angers.univ.qrludo.atom;

public class Any extends Atom{
    private String content;

    public Any(String content){
        this.content=content;
    }

    public String toString() { return "Any "+content;}

    @Override
    public String getContent() {
        return content;
    }
}
