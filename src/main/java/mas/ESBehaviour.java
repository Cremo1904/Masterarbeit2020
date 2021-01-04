package mas;

import java.util.HashMap;

public class ESBehaviour extends Behaviour {

    public double[] generateSolution(int demand, int quality, HashMap<String, Object> supplyRest, int dim, int constraint, double[] distances) {
        double[] vector = new double[dim*3];

        int[] validSupplies = new int[dim*3];
        HashMap<String, Object> angebot = new HashMap();
        boolean atLeastOne = false;
        for (int i = 0; i < dim*3; i++) {
            angebot = (HashMap) Blackboard.get(Integer.toString(i));
            int aQuality = (int) angebot.get("quality");                                        //genereller Ausschluss wenn Q-Delta > 2
            int aQuantity = (int) angebot.get("quantity");
            if (Math.abs(aQuality - quality) > 2){
                validSupplies[i] = -1;
                continue;
            }


            if (constraint == 1 || constraint == 4 || constraint == 5 || constraint == 7) {     //NF-Constraint 1: Distanz muss < 15000 Meter
                if (distances[i] > 20000.0) {
                    validSupplies[i] = -1;
                    continue;
                }
            }
            if (constraint == 2 || constraint == 4 || constraint == 6 || constraint == 7) {     //NF-Constraint 2: Nur Angebote mit Einheiten >= 50
                if (aQuantity < 50) {
                    validSupplies[i] = -1;
                    continue;
                }
            }
            if (constraint == 3 || constraint == 5 || constraint == 6 || constraint == 7) {     //NF-Constraint 3: Gleiche Q
                if (aQuality != quality) {
                    validSupplies[i] = -1;
                    continue;
                }
            }


            int aConstraint = (int) angebot.get("constraint");
            if (aConstraint == 1 || aConstraint == 4 || aConstraint == 5 || aConstraint == 7) { //AN-Constraint 1: Nur exklusive Nutzung des Angebots
                if (supplyRest.containsKey(Integer.toString(i))) {
                    int quantity = (int) supplyRest.get(Integer.toString(i));
                    if (quantity != (int) angebot.get("quantity")) {
                        validSupplies[i] = -1;
                        continue;
                    }
                }
            }
            if (aConstraint == 2 || aConstraint == 4 || aConstraint == 6 || aConstraint == 7) { //AN-Constraint 2: Nur von geraden Nachfragen
                if (i % 2 == 1) {
                    validSupplies[i] = -1;
                    continue;
                }
            }
            if (aConstraint == 3 || aConstraint == 5 || aConstraint == 6 || aConstraint == 7) { //AN-Constraint 3: Q-Delta nicht > 1
                if (Math.abs(aQuality - quality) > 1) {
                    validSupplies[i] = -1;
                    continue;
                }
            }

            atLeastOne = true;

            if (supplyRest.containsKey(Integer.toString(i))) {
                validSupplies[i] = (int) supplyRest.get(Integer.toString(i));
            } else {
                validSupplies[i] = aQuantity;
            }

        }


        if (atLeastOne) {
            ESProblem f = new ESProblem(dim, demand, distances, quality, constraint, supplyRest, validSupplies);
            boolean notASolution = true;
            while (notASolution) {
                ESProblem ESP = new ESProblem(dim, demand, distances, quality, constraint, supplyRest, validSupplies);
                ESAlgorithm ESA = new ESAlgorithm(25, dim, demand);
                ESA.solve(ESP, 5000);

                vector = ESA.finalState.vector;

                if (f.checkSolution(vector)) {
                    notASolution = false;
                }

            }
        }
        return vector;
    }


}


