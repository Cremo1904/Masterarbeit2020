package sa;

import util.Action;
import mas.Blackboard;
import util.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SAProblem implements OptimizationProblem {

    int dimensions;
    int demand;
    double[] distances;
    int quality;
    int constraint;
    HashMap<String, Object> supplyRest;
    int[] validSupplies;

    public SAProblem(int n, int d, double[] distances, int quality, int constraint, HashMap<String, Object> supplyRest, int[] validSupplies){
        this.dimensions = n;
        this.demand = d;
        this.distances = distances;
        this.quality = quality;
        this.constraint = constraint;
        this.supplyRest = supplyRest;
        this.validSupplies = validSupplies;
    }

    public int getDemand() {
        return demand;
    }

    @Override
    public State initialState() {
        return (new SAState(dimensions*3, demand));
    }

    @Override
    public ArrayList<Action> actions(State s) {
        SAState sas = (SAState)s;
        ArrayList<Action> actions = new ArrayList<>();

        Random rnd = new Random();
        double speed = Math.round(rnd.nextDouble()*(demand*0.2));
        if (speed == 0) {
            speed = 1;
        }
        double speed2 = Math.round(rnd.nextDouble()*(demand*0.2));
        if (speed2 == 0) {
            speed2 = 1;
        }

        int i = rnd.nextInt(dimensions*3);
        while (this.validSupplies[i] == -1) {
            i = rnd.nextInt(dimensions*3);
        }
        int maxQuan = validSupplies[i];
        double qoi = sas.get(i);
        double to1;
        double to2;
        if (speed < qoi) {
            to1 = qoi-speed;
        } else {
            to1 = 0;
        }
        if ((speed + qoi) < maxQuan) {
            to2 = qoi + speed;
        } else {
            to2 = maxQuan;
        }
        int j = rnd.nextInt(dimensions*3);
        while (this.validSupplies[j] == -1) {
            j = rnd.nextInt(dimensions*3);
        }
        int maxQuan2 = validSupplies[j];
        double qoi2 = sas.get(j);
        if (j == i) {
            qoi2 = to2;
        }
        if (speed2 < qoi2) {
            actions.add(new SAAction(i, qoi, to2, j, qoi2, qoi2-speed2));
        } else {
            actions.add(new SAAction(i, qoi, to2, j, qoi2, 0));
        }
        if ((speed2 + qoi2) < maxQuan2) {
            actions.add(new SAAction(i, qoi, to1, j, qoi2, qoi2+speed2));
        } else {
            actions.add(new SAAction(i, qoi, to1, j, qoi2, maxQuan2));
        }

        return actions;
    }

    @Override
    public ArrayList<State> result(State s, Action a) {
        SAState sas = (SAState)s;
        SAAction action = (SAAction)a;
        //clone current state
        SAState newstate = new SAState(dimensions*3, demand);
        for (int i = 0; i < dimensions*3; i++) {
            newstate.set(i,sas.get(i));
        }
        //apply new changes
        newstate.set(action.dim, action.to);
        newstate.set(action.dim2, action.to2);
        ArrayList<State> singleState = new ArrayList<>();
        singleState.add(newstate);
        return singleState;
    }

    @Override
    public double eval(State s) {

        double count = 0;
        double obj = 0;
        int aQuality;
        SAState sas = (SAState)s;
        double[] position = sas.getVector();
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < this.dimensions*3; i++) {
            count += position[i];
        }
        if (count > (double)this.demand) {                 //hohe Strafkosten für jede Einheit über Quantity
            obj = count;
        } else {                                        //hier eigentliche Berechnung der Fitness
            double distCost = 0;
            double qualCost = 0;
            double amount = 0;
            for (int i = 0; i < this.dimensions*3; i++) {
                if (position[i] > 0) {
                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
                    double dist = this.distances[i];
                    double delta = Math.abs(this.quality - aQuality);
                    if (delta == 0) {
                        delta = 0.0;
                    } else if (delta == 1) {
                        delta = 0.5;
                    } else {
                        delta = 1.0;
                    }
                    distCost += dist * position[i];
                    qualCost += delta * position[i];
                    amount += position[i];
                }
            }
            if (amount > 0) {
                obj += 0.47 * (distCost / (140000 * this.demand)) + 0.03 * (qualCost / this.demand) + 0.5 * ((this.demand - amount) / this.demand);
            } else {
                obj += 0.5;
            }
        }
        return obj;
    }


    public boolean checkSolution(double[] position) {
        double count = 0;
        for (int i = 0; i < this.dimensions*3; i++) {
            count += position[i];
        }
        if (count > (double)this.demand) {
            return false;
        } else {
            return true;
        }
    }


}

class SAState implements State{

    private double[] vector;

    public SAState(int n, int demand){
        vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = 0;  //demand/2;
        }
    }

    public double[] getVector() {
        return vector;
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
    double from;
    double to;
    int dim2;
    double from2;
    double to2;

    public SAAction(int dim, double from, double to, int dim2, double from2, double to2){
        this.dim = dim;
        this.from = from;
        this.to = to;
        this.dim2 = dim2;
        this.from2 = from2;
        this.to2 = to2;
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

