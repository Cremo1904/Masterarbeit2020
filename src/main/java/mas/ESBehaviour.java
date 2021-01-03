package mas;

import java.util.HashMap;

public class ESBehaviour extends Behaviour {

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
            validSupplies[i] = i;
        }


        ESProblem f = new ESProblem(dim, demand, distances, quality, constraint, supplyRest, validSupplies);
        boolean notASolution = true;
        while (notASolution) {
            ESProblem ESP= new ESProblem(dim, demand, distances, quality, constraint, supplyRest, validSupplies);
            ESAlgorithm ESA = new ESAlgorithm(25, dim, demand);
            ESA.solve(ESP,5000);

            vector = ESA.finalState.vector;

            if (f.checkSolution(vector)) {
                notASolution = false;
            }

        }
        return vector;
    }


}


