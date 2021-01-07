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
        foodSources = new ArrayList<ABCFood>();
        rnd = new Random();
        boolean done = false;
        generation = 0;

        initialize();
        memorizeBestFoodSource();

        while(!done) {
            if(generation < maxGenerations) {
                sendEmployedBees();
                getFitness();
                calculateProbabilities();
                sendOnlookerBees();
                memorizeBestFoodSource();
                sendScoutBees();

                generation++;
            } else {
                done = true;
            }

        }


        System.out.println("--- BEST SOLUTION ---");
        System.out.println("cost:" + bestFoodSource.getCost());
        //for(int i = 0; i < dim*3; i++) {
        //    System.out.println("n = " + (i+1) + " : " + bestFoodSource.getVector(i));
        //}

    }

    public void initialize() {
        int j = 0;

        for(int i = 0; i < numberOfFoodSources; i++) {
            ABCFood newHoney = new ABCFood(dim, validSupplies);

            foodSources.add(newHoney);
            j = foodSources.indexOf(newHoney);

            computeCost(foodSources.get(j));                                                                            //foodSources.get(j).computeCost();
        }
    }

    public void computeCost(ABCFood abcf) {
        double[] vector = abcf.getVector();
        double calcCost = fitFunc.eval(vector);
        abcf.setCost(calcCost);
    }

    public void memorizeBestFoodSource() {
        bestFoodSource = Collections.min(foodSources);
    }

    public void sendEmployedBees() {
        int neighborBeeIndex = 0;
        ABCFood currentBee = null;
        ABCFood neighborBee = null;

        for(int i = 0; i < numberOfFoodSources; i++) {
            neighborBeeIndex = rnd.nextInt(numberOfFoodSources);                                                                     //getExclusiveRandomNumber(FOOD_NUMBER-1, i);
            while (neighborBeeIndex == i) {
                neighborBeeIndex = rnd.nextInt(numberOfFoodSources);
            }
            currentBee = foodSources.get(i);
            neighborBee = foodSources.get(neighborBeeIndex);
            sendOut(currentBee, neighborBee);
        }
    }

    public void sendOnlookerBees() {
        int i = 0;
        int t = 0;
        int neighborBeeIndex = 0;
        ABCFood currentBee = null;
        ABCFood neighborBee = null;

        while(t < numberOfFoodSources) {
            currentBee = foodSources.get(i);
            if(rnd.nextDouble() < currentBee.getSelectionProbability()) {
                t++;
                neighborBeeIndex = rnd.nextInt(numberOfFoodSources);                                                                    //getExclusiveRandomNumber(FOOD_NUMBER-1, i);
                while (neighborBeeIndex == i) {
                    neighborBeeIndex = rnd.nextInt(numberOfFoodSources);
                }
                neighborBee = foodSources.get(neighborBeeIndex);
                sendOut(currentBee, neighborBee);
            }
            i++;
            if(i == numberOfFoodSources) {
                i = 0;
            }
        }
    }

    public void sendOut(ABCFood currentBee, ABCFood neighborBee) {
        double newValue = 0.0;
        double tmpValue = 0.0;
        double prevCost = 0.0;
        double currCost = 0.0;

        prevCost = currentBee.getCost();

        int index = rnd.nextInt(dim*3);                                                                                                    //getRandomNumber(0, MAX_LENGTH-1);
        while (validSupplies[index] < 1) {
            index = rnd.nextInt(dim*3);
        }


        tmpValue = currentBee.getVector(index);
        newValue = tmpValue + Math.round((tmpValue - neighborBee.getVector(index)) * (rnd.nextDouble()-0.5) * 2);                 //hier mutation festlegen
        /*
        if (rnd.nextDouble() > 0.5) {
            newValue = tmpValue + 1;//Math.round(rnd.nextDouble()*(demand*0.2));
        } else {
            newValue = tmpValue - 1;//Math.round(rnd.nextDouble()*(demand*0.2));
        }

         */

        if(newValue < 0) {
            newValue = 0;
        }
        if(newValue > validSupplies[index]) {                                                                                          //hier max value = demand oder validSupplies
            newValue = validSupplies[index];
        }

        currentBee.setVector(index, newValue);

        /*
        double[] vector = currentBee.getVector();
        double count = 0;
        for (int i = 0; i < dim*3; i++) {
            count += vector[i];
        }
        if (count > demand) {
            for (int j = 0; j < dim*3; j++) {
                //vector[j] = Math.round(vector[j] * 0.5);
                if (vector[j] > 0) {
                    vector[j] -= 1;
                }
            }
        }
        currentBee.setVector(vector);

         */


        computeCost(currentBee);                                                                                                                    //currentBee.computeCost();
        currCost = currentBee.getCost();

        if(prevCost < currCost) {
            currentBee.setVector(index, tmpValue);
            computeCost(currentBee);                                                                                                                //currentBee.computeCost();
            currentBee.setTrials(currentBee.getTrials() + 1);
        } else {
            currentBee.setTrials(0);
        }
    }

    public void sendScoutBees() {
        ABCFood currentBee = null;

        for(int i = 0; i < numberOfFoodSources; i++) {
            currentBee = foodSources.get(i);
            if (currentBee.getTrials() > trialLimit) {
                currentBee.initVector();
                computeCost(currentBee);                                                                                                                    //currentBee.computeCost();
                currentBee.setTrials(0);
            }
        }
    }

    public void getFitness() {
        ABCFood currentBee = null;
        double cost = 0;
        double fitness = 0;

        for(int i = 0; i < numberOfFoodSources; i++) {
            currentBee = foodSources.get(i);
            cost = currentBee.getCost();

            if (cost >= 0) {
                fitness = 1.0 / ( 1.0 + cost);
            } else {
                fitness= 1.0 - cost; // 1.0 + abs(cost)
            }
            currentBee.setFitness(fitness);
        }
    }

    public void calculateProbabilities() {
        ABCFood currentBee = null;
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

}
