package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class ClearNodes extends Action {

    public ClearNodes(MainActivity mainActivity, int nodeID) {
        super(mainActivity, nodeID);
    }

    @Override
    public String toString() {
        return "ClearNodes";
    }
}
