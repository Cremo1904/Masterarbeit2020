package es;

import util.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * operations to be performed during algorithm run
 * @author Armin Kazemi, original code: github.com/arminkz/OptimizationAlgorithms
 *
 * modified by
 * @author Lukas Cremers
 */
public class ESProblem implements GeneticProblem {

    int dimensions;
    int demand;
    double[] distances;
    int quality;
    int constraint;
    HashMap<String, Object> supplyRest;
    int[] validSupplies;
    double calls = 0;
    int[] qualities;

    public ESProblem(int n, int d, double[] distances, int quality, int constraint, HashMap<String, Object> supplyRest, int[] validSupplies, int[] qualities){
        this.dimensions = n;
        this.demand = d;
        this.distances = distances;
        this.quality = quality;
        this.constraint = constraint;
        this.supplyRest = supplyRest;
        this.validSupplies = validSupplies;
        this.qualities = qualities;
    }

    /** create initial population */
    @Override
    public ArrayList<EvolutionaryState> initialPopulation(int size) {
        ArrayList<EvolutionaryState> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            EvolutionaryState mes = new EvolutionaryState(dimensions*3);
            for (int j = 0; j < dimensions*3; j++) {
                mes.vector[j] = 0;
            }
            result.add(mes);
        }
        return result;
    }

    /** evaluate fitness for state */
    @Override
    public double fitness(EvolutionaryState mes) {
        this.calls++;
        double obj = 0;
        int aQuality;
        double[] position = mes.vector;
            double distCost = 0;
            double qualCost = 0;
            double amount = 0;
            for (int i = 0; i < this.dimensions*3; i++) {
                if (position[i] > 0) {
                    aQuality = this.qualities[i];
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
            if (amount > (double)this.demand) {
                obj = amount;
            } else {
                if (amount > 0) {
                    obj += 0.47 * (distCost / (140000 * this.demand)) + 0.03 * (qualCost / this.demand) + 0.5 * ((this.demand - amount) / this.demand);
                } else {
                    obj += 0.5;
                }
            }
        return obj;

    }

    /** check if solution is valid */
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

    /** perform crossover on two individuals */
    @Override
    public EvolutionaryState crossover(double[] v1, double[] v2) {
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

    /** perform mutation on individual */
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

    public double getCalls() {
        return this.calls;
    }

}

/**
 * solution vector
 * @author Armin Kazemi, original code: github.com/arminkz/OptimizationAlgorithms
 *
 * modified by
 * @author Lukas Cremers
 */
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


