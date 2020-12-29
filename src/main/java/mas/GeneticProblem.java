package mas;

import java.util.ArrayList;

public interface GeneticProblem {

    ArrayList<EvolutionaryState> initialPopulation(int size);

    double fitness(EvolutionaryState s);

    EvolutionaryState crossover(double[] v1, double[] v2);

    EvolutionaryState mutate(EvolutionaryState s);

}
