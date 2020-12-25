package mas;

import java.util.ArrayList;
import java.util.Random;

import java.util.ArrayList;

public class SAProblem implements OptimizationProblem {

    int dimensions;
    int demand;

    public SAProblem(int n, int d){
        this.dimensions = n;
        this.demand = d;
    }

    @Override
    public State initialState() {
        return (new SAState(dimensions, demand));
    }

    @Override
    public ArrayList<Action> actions(State s) {
        SAState sas = (SAState)s;
        ArrayList<Action> actions = new ArrayList<>();
        for (int i = 0; i < dimensions; i++) {
            int qoi = (int)sas.get(i);
            if (qoi != demand && qoi != 0) {
                actions.add(new SAAction(i, qoi, qoi+1));
                actions.add(new SAAction(i, qoi, qoi-1));
            } else if (qoi != demand) {
                actions.add(new SAAction(i, qoi, qoi+1));
            } else {
                actions.add(new SAAction(i, qoi, qoi-1));
            }
        }
        return actions;
    }

    @Override
    public ArrayList<State> result(State s, Action a) {
        SAState sas = (SAState)s;
        SAAction action = (SAAction)a;
        //clone current state
        SAState newstate = new SAState(dimensions, demand);
        for (int i = 0; i < dimensions; i++) {
            newstate.set(i,sas.get(i));
        }
        //apply new changes
        newstate.set(action.dim, action.to);
        ArrayList<State> singleState = new ArrayList<>();
        singleState.add(newstate);
        return singleState;
    }

    @Override
    public int eval(State s) {
        ChessBoardState cbs = (ChessBoardState)s;
        int hitcount = 0;
        for (int col = 0; col < N; col++) {
            int row = cbs.get(col);
            for (int o= 0; o < N; o++) {
                if(o != col){
                    //check horizontal hits
                    if(cbs.get(o) == row) hitcount++;
                    //no need to check vertical hits because of proper definition
                    //check diagonal hits
                    if(Math.abs(row - cbs.get(o)) == Math.abs(col - o)) hitcount++;
                }
            }
        }
        return hitcount / 2;
    }
}

class SAState implements State{

    private double[] vector;

    public SAState(int n, int demand){
        vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = demand/2;
        }
    }

    public double get(int i){
        return vector[i];
    }

    public void set(int i,double val){
        vector[i] = val;
    }

    @Override
    public boolean isEquals(State s) {
        return false;
    }
}

class SAAction implements Action{

    int dim;
    int from;
    int to;

    public SAAction(int dim, int from, int to){
        this.dim = dim;
        this.from = from;
        this.to = to;
    }


    @Override
    public String description() {
        return "Move Queen of Column";
    }

    @Override
    public int actionCode() {
        return 0;
    }
}
