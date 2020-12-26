package mas;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {

    //Cooling rate
    double coolingRate;

    public SimulatedAnnealing(double coolingRate){
        solution = new ArrayList<>();
        finalState = null;
        this.coolingRate = coolingRate;
    }

    private ArrayList<Action> solution;
    public SAState finalState;

    public void solve(OptimizationProblem op, SimulatedAnnealingStrategy strategy , boolean maximize){

        State currentState = op.initialState();

        //Set initial temp
        double temp = 100;
        SAProblem prob = (SAProblem)op;
        int count = 1;
        while(true){
            count += 1;
            if (count > 25000) {
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
            if (quantity > (double)prob.getDemand()) {
                for (int i = 0; i < vector.length; i++) {
                    vector[i] = Math.round(vector[i] * 0.5);
                }
            }

            double tval = op.eval(psa.getKey());
            double p = acceptanceProbability(curval,tval,temp,maximize);
            double d = rnd.nextDouble();
            //System.out.println(p + " ! " + d);
            if(p > d){
                currentState = psa.getKey();
                solution.add(psa.getValue());
                System.out.println("[SA] Eval : " + tval + "  Count: " + count + "   Temp: " + temp);
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

    }

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
