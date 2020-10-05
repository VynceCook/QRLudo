package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class AddNode extends Action {
    private int nodeToAddID;

    public AddNode(MainActivity mainActivity, int nodeID, int nodeToRemoveID) {
        super(mainActivity, nodeID);
        nodeToAddID = nodeToRemoveID;
    }
}
