package es;

import mas.Blackboard;
import util.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class ESProblem implements GeneticProblem {

    int dimensions;
    int demand;
    double[] distances;
    int quality;
    int constraint;
    HashMap<String, Object> supplyRest;
    int[] validSupplies;

    public ESProblem(int n, int d, double[] distances, int quality, int constraint, HashMap<String, Object> supplyRest, int[] validSupplies){
        this.dimensions = n;
        this.demand = d;
        this.distances = distances;
        this.quality = quality;
        this.constraint = constraint;
        this.supplyRest = supplyRest;
        this.validSupplies = validSupplies;
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
        int aConstraint;
        double[] position = mes.vector;
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
                    /*
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

                     */
                    //if (constraintsViolated == 0) {
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
                    //} else {
                    //    //obj += 1;
                    //    obj += constraintsViolated;
                    //}
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
        //constraintsViolated += checkConstraints(quantity, quality, demand, edges, constraint, supplyAlreadyUsed);
        return constraintsViolated;

    }

    public double checkConstraints(int quantity, int quality, double demand, int edges, int constraint, boolean supplyAlreadyUsed) {
        double constraintsViolated = 0;
        switch(this.constraint) {
            case 1:
                if (edges > 0) {
                    constraintsViolated += 1 * edges;
                }
                break;
            case 2:
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += (1-(double)quantity/demand);
                }
                break;
            case 3:
                if (this.quality != quality) {
                    constraintsViolated += 0.1;
                }
                break;
            case 4:
                if (edges > 0) {
                    constraintsViolated +=1 * edges;
                }
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += (1-(double)quantity/demand);
                }
                break;
            case 5:
                if (edges > 0) {
                    constraintsViolated += 1 * edges;
                }
                if (this.quality != quality) {
                    constraintsViolated += 0.1;
                }
                break;
            case 6:
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += (1-(double)quantity/demand);
                }
                if (this.quality != quality) {
                    constraintsViolated += 0.1;
                }
                break;
            case 7:
                if (edges > 0) {
                    constraintsViolated += 1 * edges;
                }
                if (((double)quantity / demand) < 0.7) {
                    constraintsViolated += (1-(double)quantity/demand);
                }
                if (this.quality != quality) {
                    constraintsViolated += 0.1;
                }
                break;
            case 8:
                break;
        }
        switch(constraint) {
            case 1:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1;
                }
                break;
            case 2:
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += (1-demand/(double)quantity);
                }
                break;
            case 3:
                if (this.quality != quality) {
                    constraintsViolated += 1;
                }
                break;
            case 4:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1;
                }
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += (1-demand/(double)quantity);
                }
                break;
            case 5:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1;
                }
                if (this.quality != quality) {
                    constraintsViolated += 1;
                }
                break;
            case 6:
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += (1-demand/(double)quantity);
                }
                if (this.quality != quality) {
                    constraintsViolated += 1;
                }
                break;
            case 7:
                if (supplyAlreadyUsed) {
                    constraintsViolated += 1;
                }
                if ((demand / (double)quantity) < 0.7) {
                    constraintsViolated += (1-demand/(double)quantity);
                }
                if (this.quality != quality) {
                    constraintsViolated += 1;
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
            /*
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

             */
            return true;
        }
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
        while (validSupplies[mindex] == -1) {
            mindex = rnd.nextInt(dimensions*3);
        }
        int maxQuan = validSupplies[mindex];
        int mindex2 = rnd.nextInt(dimensions*3);
        while (validSupplies[mindex2] == -1) {
            mindex2 = rnd.nextInt(dimensions*3);
        }
        int maxQuan2 = validSupplies[mindex2];

        if (rnd.nextDouble() > 0.5) {
            mes.vector[mindex] += 1;
        } else {
            mes.vector[mindex] -= 1;
        }
        if (rnd.nextDouble() > 0.5) {
            mes.vector[mindex2] += 1;
        } else {
            mes.vector[mindex2] -= 1;
        }
        if (mes.vector[mindex] > maxQuan) {
            mes.vector[mindex] = maxQuan;
        } else if (mes.vector[mindex] < 0) {
            mes.vector[mindex] = 0;
        }
        if (mes.vector[mindex2] > maxQuan2) {
            mes.vector[mindex2] = maxQuan2;
        } else if (mes.vector[mindex2] < 0) {
            mes.vector[mindex2] = 0;
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


