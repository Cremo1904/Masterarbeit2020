package mas;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class EvolutionaryAlgorithm {

    private int population_size;
    int dim;
    int demand;

    public EvolutionaryAlgorithm(int population_size, int dim, int demand){
        this.population_size = population_size;
        this.dim = dim;
        this.demand = demand;
    }

    public State finalState;

    public void solve(GeneticProblem gp,int generations){

        //initialization
        ArrayList<State> population = gp.initialPopulation(population_size);
        EvolutionaryState best = null;
        int k = 0;
        while(k < generations) {

            Random rnd = new Random();

            //crossover
            ArrayList<State> co_population = new ArrayList<>();
            for (int i = 0; i < population_size*2; i++) {
                int j = rnd.nextInt(population_size);
                int l = rnd.nextInt(population_size);
                co_population.add(gp.crossover(population.get(j), population.get(l)));
            }

            //mutate
            ArrayList<State> mut_population = new ArrayList<>();
            for (int i = 0; i < population_size*2; i++) {
                mut_population.add(gp.mutate(co_population.get(i)));
            }

            //re-add parents for elitist strategy
            //for (State s: population) {
            //    mut_population.add(s);
            //}
            if (best != null) mut_population.add(best);

            //evaluate
            double fitness_sum = 0;
            double bestFitness = Double.MAX_VALUE;
            double worstFitness = 0;

            ArrayList<Pair<State, Double>> populationFitness = new ArrayList<>();
            for (State p : mut_population) {

                //prüfen ob zu viele einheiten verteilt
                EvolutionaryState es = (EvolutionaryState) p;
                double count = 0;
                for (int i = 0; i < dim*3; i++) {
                    count += es.vector[i];
                }
                if (count > demand) {
                    for (int j = 0; j < dim*3; j++) {
                        es.vector[j] = es.vector[j] * 0.5;
                    }
                }

                double fitness = gp.fitness(p);
                populationFitness.add(new Pair<>(p, fitness));
                fitness_sum += fitness;
                if(fitness > worstFitness) worstFitness = fitness;
                if(fitness < bestFitness) {
                    bestFitness = fitness;
                    best = es;
                }
            }
            double avgFitness = fitness_sum / population.size();
            //System.out.println("Best Fitness generation " + k + ":  " + bestFitness);


            //Sortieren der Fitness-Liste
            Collections.sort(populationFitness, new Comparator<Pair<State, Double>>() {
                @Override
                public int compare(final Pair<State, Double> o1, final Pair<State, Double> o2) {
                    if (o1.getValue() > o2.getValue()) {
                        return 1;
                    } else if (o1.getValue().equals(o2.getValue())) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });

            //Select Population
            ArrayList<State> newPopulation = new ArrayList<>();
            while (newPopulation.size() < population_size) {
                for (int i = 0; i < population_size; i++) {
                    newPopulation.add(populationFitness.get(i).getKey());
                }
            }

            //update population
            population = newPopulation;
            k++;

        }

        //after k iterations return answer
        double bestFitness = Double.MIN_VALUE;
        State bestState = null;

        for(State s : population){
            if(bestState == null || gp.fitness(s) < bestFitness){
                bestState = s;
                bestFitness = gp.fitness(s);
            }
        }

        finalState = bestState;
        System.out.println("[GA] Final State Reached !");
    }




}