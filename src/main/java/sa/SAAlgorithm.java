package sa;

import javafx.util.Pair;
import util.Action;
import util.State;
import java.util.ArrayList;
import java.util.Random;

/**
 * the simulated annealing algorithm
 * @author Armin Kazemi, original code: github.com/arminkz/OptimizationAlgorithms
 *
 * modified by
 * @author Lukas Cremers
 */
public class SAAlgorithm {

    //Cooling rate
    double coolingRate;

    public SAAlgorithm(double coolingRate){
        solution = new ArrayList<>();
        finalState = null;
        this.coolingRate = coolingRate;
    }

    private ArrayList<Action> solution;
    public SAState finalState;

    /** start searching process */
    public void solve(OptimizationProblem op, SimulatedAnnealingStrategy strategy , boolean maximize){

        State currentState = op.initialState();

        //Set initial temp
        double temp = 1;
        SAProblem prob = (SAProblem)op;
        int count = 1;
        double result = 1000;
        while(true){
            count += 1;
            if (count > 100000) {
                break;
            }
            //Get Neighbours
            ArrayList<Pair<State,Action>> neighbours = new ArrayList<>();
            for(Action act : op.actions(currentState)){
                for(State target : op.result(currentState,act)) {
                    neighbours.add(new Pair(target,act));
                }
            }

            double curval = op.eval(currentState);
            //boolean isFound = false;

            Random rnd = new Random();
            int index = rnd.nextInt(neighbours.size());
            Pair<State,Action> psa = neighbours.get(index);

            SAState newstate = (SAState)psa.getKey();
            double[] vector = newstate.getVector();
            double quantity = 0;
            for (int i = 0; i < vector.length; i++) {
                quantity += vector[i];
            }

            double tval = 10000;
            double p = 0;
            if (quantity <= (double)prob.getDemand()) {
                tval = op.eval(psa.getKey());
                p = acceptanceProbability(curval, tval, temp, maximize);
            }
            double d = rnd.nextDouble();
            if(p > d){
                currentState = psa.getKey();
                solution.add(psa.getValue());
                result = tval;
            }

            if(strategy == SimulatedAnnealingStrategy.EXPOTENTIAL) {

                //cool system
                temp *= 1 - coolingRate;

            }else if(strategy == SimulatedAnnealingStrategy.LINEAR_TEMPERATURE) {

                temp -= coolingRate;

            }else if(strategy == SimulatedAnnealingStrategy.RANDOM_REDUCE){

                temp -= rnd.nextDouble() * coolingRate;

            }else{

                System.err.println("Invalid Strategy !");
                return;
            }

            finalState = (SAState)currentState;

        }
        System.out.println("[SA] Eval : " + result);

    }

    /** calculate acceptance probability for generated solution*/
    private static double acceptanceProbability(double currentDistance, double newDistance, double temperature , boolean maximize) {
        if(!maximize){
            // If the new solution is better, accept it
            if (newDistance < currentDistance) {
                return 1.0;
            }
            // If the new solution is worse, calculate an acceptance probability
            return Math.exp((currentDistance - newDistance) / temperature);
        }else{
            // If the new solution is better, accept it
            if (newDistance > currentDistance) {
                return 1.0;
            }
            // If the new solution is worse, calculate an acceptance probability
            return Math.exp((newDistance - currentDistance) / temperature);
        }
    }

}

