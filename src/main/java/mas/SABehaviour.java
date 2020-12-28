package mas;


import java.util.HashMap;
import ec.*;

public class SABehaviour extends Behaviour {

    public double[] generateSolution(int demand, int quality, HashMap<String, Object> supplyRest, int dim, int constraint, double[] distances) {
        double[] vector = new double[dim*3];

        int[] validSupplies = new int[dim*3];
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < dim*3; i++) {
            angebot = (HashMap) Blackboard.get(Integer.toString(i));
            int aQuality = (int) angebot.get("quality");
            if (Math.abs(aQuality - quality) > 2){
                validSupplies[i] = -1;
                continue;
            }
            if (constraint == 3 || constraint == 5 || constraint == 6 || constraint == 7) {
                if (aQuality != quality) {
                    validSupplies[i] = -1;
                    continue;
                }
            }
            int aConstraint = (int) angebot.get("constraint");
            if (aConstraint == 1 || aConstraint == 4 || aConstraint == 5 || aConstraint == 7) {
                if (supplyRest.containsKey(Integer.toString(i))) {
                    int aQuantity = (int) supplyRest.get(Integer.toString(i));
                    if (aQuantity != (int) angebot.get("quantity")) {
                        validSupplies[i] = -1;
                        continue;
                    }
                }
            }
            //if (supplyRest.containsKey(Integer.toString(i))) {
            //    maxPositions[i] = (int) supplyRest.get(Integer.toString(i));
            //} else {
            //    maxPositions[i] = (int) angebot.get("quantity");
            //}
            validSupplies[i] = i;
        }








        SAProblem f = new SAProblem(dim, demand, distances, quality, constraint, supplyRest, validSupplies);
        boolean notASolution = true;
        while (notASolution) {
            SAProblem prob = new SAProblem(dim, demand, distances, quality, constraint, supplyRest, validSupplies);
            SAAlgorithm SA = new SAAlgorithm(0.0003);
            SA.solve(prob, SimulatedAnnealingStrategy.EXPOTENTIAL, false);


            vector = SA.finalState.getVector();

            if (f.checkSolution(vector)) {
                notASolution = false;
            }

        }
        return vector;
    }


}

