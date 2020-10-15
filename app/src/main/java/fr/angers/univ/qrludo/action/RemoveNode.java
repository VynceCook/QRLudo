package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class RemoveNode extends Action {

    private int nodeToRemoveID;

    public RemoveNode(MainActivity mainActivity, int nodeID, int nodeToRemoveID) {
        super(mainActivity, nodeID);
        this.nodeToRemoveID = nodeToRemoveID;
    }

    public int getNodeToAddID() {
        return nodeToRemoveID;
    }
}
