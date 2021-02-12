package rs;

import mas.Behaviour;
import mas.Blackboard;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Random Search behaviour of the OptimizeAgent
 * @author Lukas Cremers
 */
public class RSBehaviour extends Behaviour {

    public RSBehaviour (int id) {
        super(id);
    }

    /**
     * generates a solution for given inputs
     * @param demand: demanded units
     * @param quality: quality type of demand
     * @param supplyRest: rest units from all supplies
     * @param dim: number demands
     * @param constraint: constraint id
     * @param distances: calculated distances for all possible paths
     * @return solution vector
     */
    @Override
    public double[] generateSolution(int demand, int quality, HashMap<String, Object> supplyRest, int dim, int constraint, double[] distances) {
        double[] vector = new double[dim*3];
        int edges = 0;
        MersenneTwister rng = new MersenneTwister();
        while (demand > 0) {
            ArrayList<String> list = new ArrayList<String>();
            boolean success = false;
            int m = -1;
            int i = 0;
            while (i < (dim * 3)) {
                while (true) {
                    m = rng.nextInt(dim * 3);
                    if (!list.contains(Integer.toString(m))) {
                        break;
                    }
                }
                if (matching(quality, supplyRest, m, constraint, distances[m])) {
                    success = true;
                    break;
                }
                list.add(Integer.toString(m));
                i++;
            }


            if (success && m != -1) {
                HashMap<String, Object> angebot = new HashMap();
                angebot = (HashMap) Blackboard.get(Integer.toString(m));
                edges += 1;
                int einheitenAngebot = (int) angebot.get("quantity");
                if (supplyRest.containsKey(Integer.toString(m))) {
                    if (((int) supplyRest.get(Integer.toString(m)) - demand) > 0) {
                        vector[m] = (double)demand;
                        demand = 0;
                    } else {
                        vector[m] = (double)((int)supplyRest.get(Integer.toString(m)));
                        demand = demand - (int) supplyRest.get(Integer.toString(m));
                    }
                } else {
                    if ((einheitenAngebot - demand) > 0) {
                        vector[m] = (double)demand;
                        demand = 0;
                    } else {
                        vector[m] = (double)einheitenAngebot;
                        demand = demand - einheitenAngebot;
                    }
                }
            } else {
                break;
            }

        }
        return vector;
    }


    /** verifies if demand and supply are matching */
    public boolean matching(int quality, HashMap<String, Object> supplyRest, int m, int constraint, double dist) {
        HashMap<String, Object> angebot = new HashMap();
        angebot = (HashMap) Blackboard.get(Integer.toString(m));
        if (Math.abs(quality - (int) angebot.get("quality")) <= 2) { //bedeutet, erstmal grundsätzlich wählbar unabhängig von constraints
            boolean notMatching = checkConstraints(quality, supplyRest, m, constraint, dist);
            if (notMatching) {
                return false;
            }
            if (supplyRest.containsKey(Integer.toString(m))) {
                if ((int) supplyRest.get(Integer.toString(m)) > 0) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /** checks if constraints are violated */
    public boolean checkConstraints(int quality, HashMap<String, Object> supplyRest, int rn, int constraint, double dist) {
        boolean notMatching = false;
        HashMap<String, Object> angebot = new HashMap();
        angebot = (HashMap)Blackboard.get(Integer.toString(rn));
        switch(constraint) {
            case 1:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                break;
            case 2:
                if ((int)angebot.get("quantity") < 50) {
                    notMatching = true;
                }
                break;
            case 3:
                if (quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 4:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                if ((int)angebot.get("quantity") < 50) {
                    notMatching = true;
                }
                break;
            case 5:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                if (quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 6:
                if ((int)angebot.get("quantity") < 50) {
                    notMatching = true;
                }
                if (quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 7:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                if ((int)angebot.get("quantity") < 50) {
                    notMatching = true;
                }
                if (quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 8:
                break;
        }
        if (notMatching) {
            return notMatching;
        }
        switch((int)angebot.get("constraint")) {
            case 1:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                break;
            case 2:
                if (rn % 2 == 1) {
                    notMatching = true;
                }
                break;
            case 3:
                if (Math.abs(quality - (int)angebot.get("quality")) > 1) {
                    notMatching = true;
                }
                break;
            case 4:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                if (rn % 2 == 1) {
                    notMatching = true;
                }
                break;
            case 5:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                if (Math.abs(quality - (int)angebot.get("quality")) > 1) {
                    notMatching = true;
                }
                break;
            case 6:
                if (rn % 2 == 1) {
                    notMatching = true;
                }
                if (Math.abs(quality - (int)angebot.get("quality")) > 1) {
                    notMatching = true;
                }
                break;
            case 7:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                if (rn % 2 == 1) {
                    notMatching = true;
                }
                if (Math.abs(quality - (int)angebot.get("quality")) > 1) {
                    notMatching = true;
                }
                break;
            case 8:
                break;
        }
        return notMatching;
    }

}
