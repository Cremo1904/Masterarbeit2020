package mas;

import java.util.Random;

public class ABCFood implements Comparable<ABCFood> {
    private int dimensions;
    private double[] vector;
    private int trials;
    private double cost;
    private double fitness;
    private double selectionProbability;
    private int[] validSupplies;
    private Random rnd;

    public ABCFood(int n, int[] validSupplies) {
        this.dimensions = n;
        this.vector = new double[n*3];
        trials = 0;
        cost = 0.0;
        this.fitness = 0.0;
        selectionProbability = 0.0;
        this.rnd = new Random();
        this.validSupplies = validSupplies;
        initVector();
    }

    public int compareTo(ABCFood h) {
        int result = 0;
        if (this.cost < h.getCost()) {
            result = -1;
        } else if (this.cost > h.getCost()) {
            result = 1;
        }

        return result;
    }

    public void initVector() {
        for(int i = 0; i < this.dimensions*3; i++) {
            if (validSupplies[i] > 0) {
                this.vector[i] = this.rnd.nextInt(validSupplies[i]);
            } else {
                this.vector[i] = 0;
            }
        }
    }

    public double getCost() {
        return this.cost;
    }

    public void setCost (double cost) {
        this.cost = cost;
    }

    public double getVector(int index) {
        return this.vector[index];
    }

    public double[] getVector() {
        return this.vector;
    }

    public void setVector(int index, double value) {
        this.vector[index] = value;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public void setFitness(double value) {
        this.fitness = value;
    }

    public double getFitness() {
        return this.fitness;
    }

    public void setSelectionProbability(double value) {
        this.selectionProbability = value;
    }

    public double getSelectionProbability() {
        return this.selectionProbability;
    }

    public void setTrials(int value) {
        this.trials = value;
    }

    public int getTrials() {
        return this.trials;
    }

    public double RandomValue(double low, double high) {
        return (high - low) * rnd.nextDouble() + low;
    }

}
