package es;

import java.util.ArrayList;

/**
 * interface for genetic problems
 * @author Armin Kazemi, original code: github.com/arminkz/OptimizationAlgorithms
 *
 * modified by
 * @author Lukas Cremers
 */
public interface GeneticProblem {

    ArrayList<EvolutionaryState> initialPopulation(int size);

    double fitness(EvolutionaryState s);

    EvolutionaryState crossover(double[] v1, double[] v2);

    EvolutionaryState mutate(EvolutionaryState s);

}
