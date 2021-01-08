package sa;


import util.Action;
import util.State;

import java.util.ArrayList;

public interface OptimizationProblem {

    State initialState();

    ArrayList<Action> actions(State s);

    ArrayList<State> result(State s,Action a);

    double eval(State s);

}
