package mas;

import mas.GeneticProblem;
import mas.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

// Equation:
// ax + by + cz + dw = f

public class EvolutionaryProblem implements GeneticProblem {

    int dimensions;
    int demand;
    double[] distances;
    int quality;
    int constraint;
    HashMap<String, Object> supplyRest;
    int[] validSupplies;

    public EvolutionaryProblem(int n, int d, double[] distances) { //, int quality, int constraint, HashMap<String, Object> supplyRest, int[] validSupplies){
        this.dimensions = n;
        this.demand = d;
        this.distances = distances;
        //this.quality = quality;
        //this.constraint = constraint;
        //this.supplyRest = supplyRest;
        //this.validSupplies = validSupplies;
    }

    @Override
    public ArrayList<State> initialPopulation(int size) {
        ArrayList<State> result = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < size; i++) {
            EvolutionaryState mes = new EvolutionaryState(dimensions*3);
            for (int j = 0; j < dimensions*3; j++) {
                mes.vector[j] =  0; //rnd.nextInt(demand);                                        //hier evtl anpassen
            }
            result.add(mes);
        }
        return result;
    }

    @Override
    public double fitness(State s) {
        EvolutionaryState mes = (EvolutionaryState)s;

        double count = 0;
        double obj = 0;
        int aQuantity;
        int aQuality;
        double[] vector = mes.vector;
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < dimensions*3; i++) {
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
            for (int i = 0; i < dimensions*3; i++) {
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

    }

    @Override
    public State crossover(State s1, State s2) {
        EvolutionaryState mes1 = (EvolutionaryState)s1;
        EvolutionaryState mes2 = (EvolutionaryState)s2;
        EvolutionaryState result = new EvolutionaryState(dimensions*3);

        Random rnd = new Random();
        int cindex = rnd.nextInt((dimensions*3)-1) + 1;

        for (int i = 0; i < cindex; i++) {
            result.vector[i] = mes1.vector[i];
        }
        for (int i = cindex; i < dimensions*3 ; i++) {
            result.vector[i] = mes2.vector[i];
        }
        return result;
    }

    @Override
    public State mutate(State s) {
        EvolutionaryState mes = (EvolutionaryState)s;
        EvolutionaryState result = new EvolutionaryState(dimensions*3);

        Random rnd = new Random();
        int mindex = rnd.nextInt(dimensions*3);

        double from = 0;
        double speed = 0;
        for (int i = 0; i < dimensions*3; i++ ) {
            if(i != mindex) {
                result.vector[i] = mes.vector[i];
            } else {
                from = mes.vector[i];
                //speed = Math.round(rnd.nextDouble()* (demand * 0.2));
                if (speed < 1) {
                    speed = 1;
                }
                if (rnd.nextDouble() > 0.5) {
                    result.vector[i] = from + speed;
                } else {
                    result.vector[i] = from - speed;
                }
                if (result.vector[i] > demand) {
                    result.vector[i] = demand;
                } else if (result.vector[i] < 0) {
                    result.vector[i] = 0;
                }
            }
        }

        return result;
    }

}

class EvolutionaryState implements State {

    public double vector[];

    public EvolutionaryState(int n){
        vector = new double[n];
    }

    @Override
    public boolean isEquals(State s) {
        return false;
    }

    @Override
    public String toString() {
        return "";
    }
}
