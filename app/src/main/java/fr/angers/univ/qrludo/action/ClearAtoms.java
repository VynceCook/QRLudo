package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class ClearAtoms extends Action {

    public ClearAtoms (MainActivity mainActivity, int nodeID){
        super(mainActivity,nodeID);
    }

    @Override
    public String toString() {
        return "ClearAtoms";
    }
}
