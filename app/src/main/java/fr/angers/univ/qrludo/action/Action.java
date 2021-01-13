package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public abstract class Action {
    MainActivity mainActivity;
    private int nodeID;

    public Action(MainActivity mainActivity, int ID){
        this.mainActivity = mainActivity;
        this.nodeID = ID;
    }

    public abstract String toString();
}
