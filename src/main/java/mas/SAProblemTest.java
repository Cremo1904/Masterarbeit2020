package mas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SAProblemTest implements OptimizationProblem {

    int dimensions;
    int demand;
    double[] distances;

    public SAProblemTest(int n, int d, double[] distances){
        this.dimensions = n;
        this.demand = d;
        this.distances = distances;
    }

    public int getDemand() {
        return demand;
    }

    @Override
    public State initialState() {
        return (new SAStateTest(dimensions*3, demand));
    }

    @Override
    public ArrayList<Action> actions(State s) {
        SAStateTest sas = (SAStateTest)s;
        ArrayList<Action> actions = new ArrayList<>();
        /*for (int i = 0; i < dimensions; i++) {
            int qoi = (int) sas.get(i);
            if (qoi != demand && qoi != 0) {
                actions.add(new SAAction(i, qoi, demand));
                actions.add(new SAAction(i, qoi, 0));
            } else if (qoi == 0) {
                actions.add(new SAAction(i, qoi, demand));
            } else {
                actions.add(new SAAction(i, qoi, 0));
            }
        }*/

/*
        for (int i = 0; i < dimensions; i++) {                  //actions hier noch begrenzen auf wählbare angebote(dimensionen)
            int qoi = (int)sas.get(i);
            int deltaUp = demand - qoi;
            int max;
            if (maxVelocity > deltaUp) {
                max = deltaUp;
            } else {
                max = maxVelocity;
            }
            for (int j = 1; j <= max; j++) {
                actions.add(new SAAction(i, qoi, qoi+j));
            }
            if (maxVelocity > qoi) {
                max = qoi;
            } else {
                max = maxVelocity;
            }
            for (int j = 1; j <= max; j++) {
                actions.add(new SAAction(i, qoi, qoi-j));
            }
        }

 */
        Random rnd = new Random();
        double speed = Math.round(rnd.nextDouble()*(demand*0.2));
        double speed2 = Math.round(rnd.nextDouble()*(demand*0.2));
        if (speed == 0) {
            speed = 1;
        }
        if (speed2 == 0) {
            speed2 = 1;
        }


        //for (int i = 0; i < dimensions; i++) {
        int i = rnd.nextInt(dimensions*3);
        double qoi = sas.get(i);
            double to1;
            double to2;
            if (speed < qoi) {
                //actions.add(new SAAction(i, qoi, qoi-speed));
                to1 = qoi-speed;
            } else {
                //actions.add(new SAAction(i, qoi, 0));
                to1 = 0;
            }
            if ((speed + qoi) < demand) {
                //actions.add(new SAAction(i, qoi, qoi+speed));
                to2 = qoi + speed;
            } else {
                //actions.add(new SAAction(i, qoi, demand));
                to2 = demand;
            }
            int j = rnd.nextInt(dimensions*3);
            while (j == i) {
                j = rnd.nextInt(dimensions*3);
            }
            double qoi2 = sas.get(j);
            if (j != i) {
                if (speed2 < qoi2) {
                    actions.add(new SAActionTest(i, qoi, to2, j, qoi2, qoi2-speed2));
                } else {
                    actions.add(new SAActionTest(i, qoi, to2, j, qoi2, 0));
                }
                if ((speed2 + qoi2) < demand) {
                    actions.add(new SAActionTest(i, qoi, to1, j, qoi2, qoi2+speed2));
                } else {
                    actions.add(new SAActionTest(i, qoi, to1, j, qoi2, demand));
                }
            }
        //}



        /*for (int i = 0; i < dimensions; i++) {
            int qoi = (int)sas.get(i);
            if (qoi != demand && qoi != 0) {
                actions.add(new SAAction(i, qoi, qoi+1));
                actions.add(new SAAction(i, qoi, qoi-1));
            } else if (qoi != demand) {
                actions.add(new SAAction(i, qoi, qoi+1));
            } else {
                actions.add(new SAAction(i, qoi, qoi-1));
            }
        }*/
        return actions;
    }

    @Override
    public ArrayList<State> result(State s, Action a) {
        SAStateTest sas = (SAStateTest)s;
        SAActionTest action = (SAActionTest)a;
        //clone current state
        SAStateTest newstate = new SAStateTest(dimensions*3, demand);
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
        SAStateTest sas = (SAStateTest)s;
        double[] vector = sas.getVector();
        //double aLon;
        //double aLat;
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < 300; i++) {
            count += vector[i];
        }
        if (count > (double)this.demand) {                 //hohe Strafkosten für jede Einheit über Quantity
            //obj = (count + (double)this.quantity);
            //obj = (count + distances.length);
            obj = count;
            //obj = 1000;
        } else {                                        //hier eigentliche Berechnung der Fitness
            double distCost = 0;
            double qualCost = 0;
            double amount = 0;
            int edges = 0;
            for (int i = 0; i < 300; i++) {
                if (vector[i] > 0) {
                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
                    aQuantity = (int) angebot.get("quantity");
                    /*boolean supplyAlreadyUsed = false;
                    if (supplyRest.containsKey(Integer.toString(i))) {
                        aQuantity = (int) supplyRest.get(Integer.toString(i));
                        if (aQuantity != (int) angebot.get("quantity")) {
                            supplyAlreadyUsed = true;
                        }
                    } else {
                        aQuantity = (int) angebot.get("quantity");
                    }
                    aConstraint = (int)angebot.get("constraint");
                    double constraintsViolated = matching(aQuantity, aQuality, position[i], edges, aConstraint, supplyAlreadyUsed);*/
                    if (aQuantity > 0) {
                        //aLon = (double) angebot.get("lon");
                        //aLat = (double) angebot.get("lat");
                        //if (position[i] > (double) aQuantity) {
                        //    obj += 1;
                        //} else {
                        double dist = this.distances[i];
                        //double delta = Math.abs(this.quality - aQuality);
                        //if (delta == 0) {
                        //    delta = 0.0;
                        //} else if (delta == 1) {
                        //    delta = 0.5;
                        //} else {
                        //    delta = 1.0;
                        //}
                        distCost += dist * vector[i];
                        //qualCost += delta * position[i];
                        amount += vector[i];
                        edges += 1;
                        //}
                    } else {
                        obj += 1;
                    }
                }
                //if (edges > 5) {
                //    obj += edges;
                //    break;
                //}
            }
            if (amount > 0) {
                obj += 0.47 * (distCost / (140000 * this.demand)) + 0.03 * (qualCost / this.demand) + 0.5 * ((this.demand - amount) / this.demand);
            } else {
                obj += 0.5;
            }
        }
        return obj;






        /*
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

         */
    }




}

class SAStateTest implements State{

    private double[] vector;

    public SAStateTest(int n, int demand){
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

class SAActionTest implements Action{

    int dim;
    double from;
    double to;
    int dim2;
    double from2;
    double to2;

    public SAActionTest(int dim, double from, double to, int dim2, double from2, double to2){
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
