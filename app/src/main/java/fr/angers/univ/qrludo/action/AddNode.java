package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;
import fr.angers.univ.qrludo.scenario.Node;

public class AddNode extends Action {
    private int nodeToAddID;

    public AddNode(MainActivity mainActivity, int nodeID, int nodeToAddID) {
        super(mainActivity, nodeID);
        this.nodeToAddID = nodeToAddID;
    }

    public int getNodeToAddID() {
        return nodeToAddID;
    }

    public String toString() {return "AddNode "+ nodeToAddID;}
}
