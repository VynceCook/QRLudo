package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.atom.Atom;

public class AddAtom extends Action{
    private Atom atom;

    public AddAtom(MainActivity mainActivity, int nodeID, Atom atom){
        super(mainActivity,nodeID);
        this.atom = atom;
    }
}
