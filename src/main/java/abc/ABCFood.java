package abc;

import java.util.Random;

/**
 * solution candidate
 * @author Kubo Shizuma, original code: github.com/kuboshizuma/swarm-intelligence
 *
 * modified by
 * @author Lukas Cremers
 */
public class ABCFood implements Comparable<ABCFood> {
    private int dimensions;
    private double[] vector;
    private int limit;
    private double cost;
    private double fitness;
    private double selectionProbability;
    private int[] validSupplies;
    private Random rnd;

    public ABCFood(int n, int[] validSupplies) {
        this.dimensions = n;
        this.vector = new double[n*3];
        this.limit = 0;
        this.cost = 0.0;
        this.fitness = 0.0;
        this.selectionProbability = 0.0;
        this.rnd = new Random();
        this.validSupplies = validSupplies;
        initVector();
    }

    /** initiate solution vector */
    public void initVector() {
        int index = rnd.nextInt(this.dimensions*3);
        while (validSupplies[index] < 1) {
            index = rnd.nextInt(this.dimensions*3);
        }
        int quantity = this.rnd.nextInt(validSupplies[index]);
        if (quantity == 0) {
            quantity = 1;
        }
        this.vector[index] = quantity;
    }

    /** compare two food sources */
    public int compareTo(ABCFood otherFood) {
        int result = 0;
        if (this.cost < otherFood.getCost()) {
            result = -1;
        } else if (this.cost > otherFood.getCost()) {
            result = 1;
        }

        return result;
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

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return this.limit;
    }

}
