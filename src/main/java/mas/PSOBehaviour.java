package mas;

import net.sourceforge.jswarm_pso.Neighborhood;
import net.sourceforge.jswarm_pso.Neighborhood1D;
import net.sourceforge.jswarm_pso.Swarm;
import java.util.HashMap;


public class PSOBehaviour extends Behaviour {


    public double[] generateSolution(int demand, int quality, HashMap<String, Object> supplyRest, int dim, int constraint, double[] distances) {


        double[] vector = new double[dim*3];

        double[] maxPositions = new double[dim*3];
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < dim*3; i++) {
            angebot = (HashMap) Blackboard.get(Integer.toString(i));
            int aQuality = (int) angebot.get("quality");
            if (Math.abs(aQuality - quality) > 2){
                maxPositions[i] = 0;
                continue;
            }
            if (constraint == 3 || constraint == 5 || constraint == 6 || constraint == 7) {
                if (aQuality != quality) {
                    maxPositions[i] = 0;
                    continue;
                }
            }
            int aConstraint = (int) angebot.get("constraint");
            if (aConstraint == 1 || aConstraint == 4 || aConstraint == 5 || aConstraint == 7) {
                if (supplyRest.containsKey(Integer.toString(i))) {
                    int aQuantity = (int) supplyRest.get(Integer.toString(i));
                    if (aQuantity != (int) angebot.get("quantity")) {
                        maxPositions[i] = 0;
                        continue;
                    }
                }
            }
            //if (supplyRest.containsKey(Integer.toString(i))) {
            //    maxPositions[i] = (int) supplyRest.get(Integer.toString(i));
            //} else {
            //    maxPositions[i] = (int) angebot.get("quantity");
            //}
            maxPositions[i] = demand;
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
            //swarm.setMaxPosition(demand);
            swarm.setMaxPosition(maxPositions);
            swarm.setMinPosition(0);
            /*double[] minVelocity = new double[30];
            double[] maxVelocity = new double[30];
            for (int i = 0; i < 30; i++) {
                minVelocity[i] = -(0.2 * demand);
                maxVelocity[i] = 0.2 * demand;
            }
            swarm.setMinVelocity(minVelocity);
            swarm.setMaxVelocity(maxVelocity);*/
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

        //System.out.println(time2-time1);
        // Print results
        //System.out.println(swarm.toStringStats());
        //System.out.println("End: Example 1");
        return vector;
    }


}
