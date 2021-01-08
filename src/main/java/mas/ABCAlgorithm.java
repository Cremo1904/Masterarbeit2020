package mas;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;


public class ABCAlgorithm {
    public int dim;
    public int demand;
    public int[] validSupplies;
    public int numberOfBees;
    public int numberOfFoodSources;
    public int trialLimit;
    public int maxGenerations;
    public ArrayList<ABCFood> foodSources;
    public ABCFood bestFoodSource;
    public Random rnd;
    public int generation;
    public ABCFitnessFunction fitFunc;

    public ABCAlgorithm(int dim, int d, int[] validSupplies, int numberOfBees, int maxGenerations, int trialLimit, ABCFitnessFunction fitFunc) {
        this.dim = dim;
        this.demand = d;
        this.validSupplies = validSupplies;
        this.numberOfBees = numberOfBees;
        this.numberOfFoodSources = numberOfBees / 2;
        this.trialLimit = trialLimit;
        this.maxGenerations = maxGenerations;
        this.bestFoodSource = null;
        this.generation = 0;
        this.fitFunc = fitFunc;
    }

    public void algorithm() {
        foodSources = new ArrayList<>();
        rnd = new Random();
        generation = 0;
        boolean finished = false;

        initialize();
        getBestFoodSource();

        while(!finished) {
            if(generation < maxGenerations) {
                sendEmployedBees();
                getFitness();
                calculateProbabilities();
                sendOnlookerBees();
                getBestFoodSource();
                sendScoutBees();

                generation++;
            } else {
                finished = true;
            }

        }
        System.out.println("solution found:" + bestFoodSource.getCost());
    }

    public void initialize() {
        int j;
        for(int i = 0; i < numberOfFoodSources; i++) {
            ABCFood newABCFood = new ABCFood(dim, validSupplies);

            foodSources.add(newABCFood);
            j = foodSources.indexOf(newABCFood);

            calculateCost(foodSources.get(j));
        }
    }

    public void getBestFoodSource() {
        bestFoodSource = Collections.min(foodSources);
    }

    public void calculateCost(ABCFood abcFood) {
        double[] vector = abcFood.getVector();
        double cost = fitFunc.eval(vector);
        abcFood.setCost(cost);
    }

    public void sendEmployedBees() {
        int index;
        ABCFood currentBee;
        ABCFood neighborBee;

        for(int i = 0; i < numberOfFoodSources; i++) {
            index = rnd.nextInt(numberOfFoodSources);
            while (index == i) {
                index = rnd.nextInt(numberOfFoodSources);
            }
            currentBee = foodSources.get(i);
            neighborBee = foodSources.get(index);
            sendOut(currentBee, neighborBee);
        }
    }

    public void sendOnlookerBees() {
        int i = 0;
        int n = 0;
        int index;
        ABCFood currentBee;
        ABCFood neighborBee;

        while(n < numberOfFoodSources) {
            currentBee = foodSources.get(i);
            if(rnd.nextDouble() < currentBee.getSelectionProbability()) {
                n++;
                index = rnd.nextInt(numberOfFoodSources);
                while (index == i) {
                    index = rnd.nextInt(numberOfFoodSources);
                }
                neighborBee = foodSources.get(index);
                sendOut(currentBee, neighborBee);
            }
            i++;
            if(i == numberOfFoodSources) {
                i = 0;
            }
        }
    }

    public void sendScoutBees() {
        ABCFood currentBee;

        for(int i = 0; i < numberOfFoodSources; i++) {
            currentBee = foodSources.get(i);
            if (currentBee.getLimit() > trialLimit) {
                currentBee.initVector();
                calculateCost(currentBee);
                currentBee.setLimit(0);
            }
        }
    }

    public void sendOut(ABCFood currentBee, ABCFood neighborBee) {
        double newCost;
        double oldCost;
        double oldParameterValue;
        double newParameterValue;

        oldCost = currentBee.getCost();
        int index = rnd.nextInt(dim*3);
        while (validSupplies[index] < 1) {
            index = rnd.nextInt(dim*3);
        }

        oldParameterValue = currentBee.getVector(index);
        newParameterValue = oldParameterValue + Math.round((oldParameterValue - neighborBee.getVector(index)) * (rnd.nextDouble()-0.5) * 2);
        if(newParameterValue < 0) {
            newParameterValue = 0;
        }
        if(newParameterValue > validSupplies[index]) {
            newParameterValue = validSupplies[index];
        }

        currentBee.setVector(index, newParameterValue);
        calculateCost(currentBee);
        newCost = currentBee.getCost();

        if(oldCost < newCost) {
            currentBee.setVector(index, oldParameterValue);
            calculateCost(currentBee);
            currentBee.setLimit(currentBee.getLimit() + 1);
        } else {
            currentBee.setLimit(0);
        }
    }

    public void calculateProbabilities() {
        ABCFood currentBee;
        double fitnessSum = 0;

        for(int i = 1; i < numberOfFoodSources; i++) {
            currentBee = foodSources.get(i);
            fitnessSum += currentBee.getFitness();
        }

        for(int j = 0; j < numberOfFoodSources; j++) {
            currentBee = foodSources.get(j);
            currentBee.setSelectionProbability(currentBee.getFitness() / fitnessSum);
        }
    }

    public void getFitness() {
        ABCFood currentBee;
        double cost;
        double fitness;

        for(int i = 0; i < numberOfFoodSources; i++) {
            currentBee = foodSources.get(i);
            cost = currentBee.getCost();
            fitness = 1.0 / ( 1.0 + cost);
            currentBee.setFitness(fitness);
        }
    }

}
