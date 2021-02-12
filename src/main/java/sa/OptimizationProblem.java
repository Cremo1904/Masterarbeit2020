package sa;

import util.Action;
import util.State;

import java.util.ArrayList;

/**
 * interface for optimization problems
 * @author Armin Kazemi, original code: github.com/arminkz/OptimizationAlgorithms
 */
public interface OptimizationProblem {

    State initialState();

    ArrayList<Action> actions(State s);

    ArrayList<State> result(State s,Action a);

    double eval(State s);

}
