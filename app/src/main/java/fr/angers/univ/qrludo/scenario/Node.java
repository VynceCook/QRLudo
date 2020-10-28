package fr.angers.univ.qrludo.scenario;

import java.util.List;

import fr.angers.univ.qrludo.action.Action;
import fr.angers.univ.qrludo.atom.Atom;
import fr.angers.univ.qrludo.activities.MainActivity;

public class Node {

    private List<Action> Actions;
    private List<Action> TODOActions;
    public List<Atom> Conditions;
    public int ID;
    private MainActivity mainActivity;

    public Node(MainActivity mainActivity, int ID, List<Action> Actions, List<Atom> Conditions){
        this.mainActivity = mainActivity;
        this.ID = ID;
        this.Actions = Actions;
        this.Conditions = Conditions;
    }
}