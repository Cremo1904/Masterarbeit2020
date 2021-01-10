package pso;

import mas.*;
import net.sourceforge.jswarm_pso.Neighborhood;
import net.sourceforge.jswarm_pso.Neighborhood1D;
import net.sourceforge.jswarm_pso.Swarm;
import java.util.HashMap;


public class PSOBehaviour extends Behaviour {

    public PSOBehaviour (int id) {
        super(id);
    }

    @Override
    public double[] generateSolution(int demand, int quality, HashMap<String, Object> supplyRest, int dim, int constraint, double[] distances) {


        double[] vector = new double[dim*3];

        double[] maxPositions = new double[dim*3];
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < dim*3; i++) {
            angebot = (HashMap) Blackboard.get(Integer.toString(i));
            int aQuality = (int) angebot.get("quality");                                        //genereller Ausschluss wenn Q-Delta > 2
            int aQuantity = (int) angebot.get("quantity");
            if (Math.abs(aQuality - quality) > 2){
                maxPositions[i] = 0;
                continue;
            }

            if (constraint == 1 || constraint == 4 || constraint == 5 || constraint == 7) {     //NF-Constraint 1: Distanz muss < 15000 Meter
                if (distances[i] > 20000.0) {
                    maxPositions[i] = 0;
                    continue;
                }
            }
            if (constraint == 2 || constraint == 4 || constraint == 6 || constraint == 7) {     //NF-Constraint 2: Nur Angebote mit Einheiten >= 50
                if (aQuantity < 50) {
                    maxPositions[i] = 0;
                    continue;
                }
            }
            if (constraint == 3 || constraint == 5 || constraint == 6 || constraint == 7) {     //NF-Constraint 3: Gleiche Q
                if (aQuality != quality) {
                    maxPositions[i] = 0;
                    continue;
                }
            }

            int aConstraint = (int) angebot.get("constraint");
            if (aConstraint == 1 || aConstraint == 4 || aConstraint == 5 || aConstraint == 7) { //AN-Constraint 1: Nur exklusive Nutzung des Angebots
                if (supplyRest.containsKey(Integer.toString(i))) {
                    int quantity = (int) supplyRest.get(Integer.toString(i));
                    if (quantity != (int) angebot.get("quantity")) {
                        maxPositions[i] = 0;
                        continue;
                    }
                }
            }
            if (aConstraint == 2 || aConstraint == 4 || aConstraint == 6 || aConstraint == 7) { //AN-Constraint 2: Nur von geraden Nachfragen
                if (i % 2 == 1) {
                    maxPositions[i] = 0;
                    continue;
                }
            }
            if (aConstraint == 3 || aConstraint == 5 || aConstraint == 6 || aConstraint == 7) { //AN-Constraint 3: Q-Delta nicht > 1
                if (Math.abs(aQuality - quality) > 1) {
                    maxPositions[i] = 0;
                    continue;
                }
            }

            if (supplyRest.containsKey(Integer.toString(i))) {
                maxPositions[i] = (int) supplyRest.get(Integer.toString(i));
            } else {
                maxPositions[i] = aQuantity;
            }
        }



        PSOFitnessFunction f = new PSOFitnessFunction(quality, demand, dim, supplyRest, constraint, distances);
        boolean notASolution = true;
        while (notASolution) {
            // Create a swarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
            PSOParticle.DIMENSION = dim*3;
            Swarm swarm = new Swarm(25, new PSOParticle(), new PSOFitnessFunction(quality, demand, dim, supplyRest, constraint, distances));

            // Use neighborhood
            Neighborhood neigh = new Neighborhood1D(25 / 5, true);
            swarm.setNeighborhood(neigh);
            swarm.setNeighborhoodIncrement(0.9);

            // Set position (and velocity) constraints. I.e.: where to look for solutions
            swarm.setInertia(0.95);
            swarm.setMaxPosition(maxPositions);
            swarm.setMinPosition(0);
            swarm.setMaxMinVelocity(0.2 * demand);
            swarm.setParticleUpdate(new PSOParticleUpdate(swarm.getSampleParticle(), demand));

            int numberOfIterations = 100;

            for (int i = 0; i < numberOfIterations; i++) {
                swarm.evolve();
            }
            vector = swarm.getBestPosition();

            if (f.checkSolution(vector)) {
                notASolution = false;
                System.out.println("Gefundene zulässige Lösung: " + swarm.getBestFitness());
            } else {
                System.out.println("Gefundene unzulässige Lösung: " + swarm.getBestFitness());
            }
        }

        return vector;
    }


}
