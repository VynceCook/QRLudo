package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class CaptureSpeech extends Action {

    public CaptureSpeech(MainActivity mainActivity, int nodeID) {
        super(mainActivity, nodeID);
    }

    @Override
    public String toString() {
        return "CaptureSpeech";
    }
}
