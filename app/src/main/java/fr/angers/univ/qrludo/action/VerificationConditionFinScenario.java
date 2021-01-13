package fr.angers.univ.qrludo.action;

import fr.angers.univ.qrludo.activities.MainActivity;

public class VerificationConditionFinScenario extends Action {

    public VerificationConditionFinScenario (MainActivity mainActivity, int nodeID){
        super(mainActivity,nodeID);
    }

    @Override
    public String toString() {
        return "VerificationConditionFinScenario";
    }
}
