package mas;

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

        int i = rnd.nextInt(dimensions*3);                                        //actions hier noch auf wählbare begrenzen
        while (this.validSupplies[i] == -1) {
            i = rnd.nextInt(dimensions*3);
        }

        double qoi = sas.get(i);
        double to1;
        double to2;
        if (speed < qoi) {
            to1 = qoi-speed;
        } else {
            to1 = 0;
        }
        if ((speed + qoi) < demand) {
            to2 = qoi + speed;
        } else {
            to2 = demand;
        }
        int j = rnd.nextInt(dimensions*3);
        while (j == i || this.validSupplies[j] == -1) {
            j = rnd.nextInt(dimensions*3);
        }
        double qoi2 = sas.get(j);
        if (j != i) {
            if (speed2 < qoi2) {
                actions.add(new SAAction(i, qoi, to2, j, qoi2, qoi2-speed2));
            } else {
                actions.add(new SAAction(i, qoi, to2, j, qoi2, 0));
            }
            if ((speed2 + qoi2) < demand) {
                actions.add(new SAAction(i, qoi, to1, j, qoi2, qoi2+speed2));
            } else {
                actions.add(new SAAction(i, qoi, to1, j, qoi2, demand));
            }
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
        int aQuantity;
        int aQuality;
        int aConstraint;
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
            int edges = 0;
            for (int i = 0; i < this.dimensions*3; i++) {
                if (position[i] > 0) {
                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
                    boolean supplyAlreadyUsed = false;
                    if (this.supplyRest.containsKey(Integer.toString(i))) {
                        aQuantity = (int) this.supplyRest.get(Integer.toString(i));
                        if (aQuantity != (int) angebot.get("quantity")) {
                            supplyAlreadyUsed = true;
                        }
                    } else {
                        aQuantity = (int) angebot.get("quantity");
                    }
                    aConstraint = (int)angebot.get("constraint");
                    double constraintsViolated = matching(aQuantity, aQuality, position[i], edges, aConstraint, supplyAlreadyUsed);
                    if (constraintsViolated == 0) {
                        //aLon = (double) angebot.get("lon");
                        //aLat = (double) angebot.get("lat");
                        //if (position[i] > (double) aQuantity) {
                        //    obj += 1;
                        //} else {
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
                        edges += 1;
                        //}
                    } else {
                        //obj += 1;
                        obj += constraintsViolated;
                    }
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

    public double matching(int quantity, int quality, double demand, int edges, int constraint, boolean supplyAlreadyUsed) {

        double constraintsViolated = 0;
        if (Math.abs(this.quality - quality) > 2) { //bedeutet, erstmal grundsätzlich wählbar unabhängig von constraints
            constraintsViolated += 1;
        }
        if (quantity < demand) {
            constraintsViolated += 1;
        }
        constraintsViolated += checkConstraints(quantity, quality, demand, edges, constraint, supplyAlreadyUsed);
        return constraintsViolated;

    }

    public double checkConstraints(int quantity, int quality, double demand, int edges, int constraint, boolean supplyAlreadyUsed) {
        double constraintsViolated = 0;
        switch(this.constraint) {
            case 1:
                if (edges > 0) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 2:
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 3:
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 4:
                if (edges > 0) {
                    constraintsViolated += 1/dimensions;
                }
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 5:
                if (edges > 0) {
                    constraintsViolated += 1/dimensions;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 6:
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 7:
                if (edges > 0) {
                    constraintsViolated += 1/dimensions;
                }
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 8:
                break;
        }
        switch(constraint) {
            case 1:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 2:
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 3:
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 4:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1/dimensions;
                }
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 5:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1/dimensions;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 6:
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 7:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1/dimensions;
                }
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += 1/dimensions;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1/dimensions;
                }
                break;
            case 8:
                break;
        }
        return constraintsViolated;
    }


    public boolean checkSolution(double[] position) {
        HashMap<String, Object> angebot = new HashMap();
        double count = 0;
        int aQuantity;
        int aQuality;
        int aConstraint;
        for (int i = 0; i < this.dimensions*3; i++) {
            count += position[i];
        }
        if (count > (double)this.demand) {                 //hohe Strafkosten für jede Einheit über Quantity
            return false;
        } else {                                        //hier eigentliche Berechnung der Fitness
            int edges = 0;
            for (int i = 0; i < this.dimensions*3; i++) {
                if (position[i] > 0) {
                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
                    boolean supplyAlreadyUsed = false;
                    if (supplyRest.containsKey(Integer.toString(i))) {
                        aQuantity = (int) supplyRest.get(Integer.toString(i));
                        if (aQuantity != (int) angebot.get("quantity")) {
                            supplyAlreadyUsed = true;
                        }
                    } else {
                        aQuantity = (int) angebot.get("quantity");
                    }
                    aConstraint = (int)angebot.get("constraint");
                    if (matching(aQuantity, aQuality, position[i], edges, aConstraint, supplyAlreadyUsed) == 0) {
                        edges += 1;
                    } else {
                        return false;
                    }
                }
            }
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

