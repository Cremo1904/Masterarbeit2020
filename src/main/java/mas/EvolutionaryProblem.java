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
    public ArrayList<EvolutionaryState> initialPopulation(int size) {
        ArrayList<EvolutionaryState> result = new ArrayList<>();
        //Random rnd = new Random();
        for (int i = 0; i < size; i++) {
            EvolutionaryState mes = new EvolutionaryState(dimensions*3);
            for (int j = 0; j < dimensions*3; j++) {
                mes.vector[j] = 0; //rnd.nextInt(demand);                                        //hier evtl anpassen
            }
            result.add(mes);
        }
        return result;
    }

    @Override
    public double fitness(EvolutionaryState mes) {

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
    public EvolutionaryState crossover(double[] v1, double[] v2) { //State s1, State s2) {
        EvolutionaryState result = new EvolutionaryState(dimensions*3);

        Random rnd = new Random();
        int cindex = rnd.nextInt((dimensions*3)-1) + 1;
        for (int i = 0; i < cindex; i++) {
            result.vector[i] = v1[i];
        }
        for (int i = cindex; i < dimensions*3 ; i++) {
            result.vector[i] = v2[i];
        }

        return result;

    }

    @Override
    public EvolutionaryState mutate(EvolutionaryState mes) {

        Random rnd = new Random();
        int mindex = rnd.nextInt(dimensions*3);
        double from = mes.vector[mindex];
        if (rnd.nextDouble() > 0.5) {
            mes.vector[mindex] = from + 1;
        } else {
            mes.vector[mindex] = from - 1;
        }
        if (mes.vector[mindex] > demand) {
            mes.vector[mindex] = demand;
        } else if (mes.vector[mindex] < 0) {
            mes.vector[mindex] = 0;
        }
        return mes;
    }

}

class EvolutionaryState implements State {

    public double vector[];

    public EvolutionaryState(int n){
        vector = new double[n];
    }

    public EvolutionaryState(double[] vector){
        this.vector = vector;
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
