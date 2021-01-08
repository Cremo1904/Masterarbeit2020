package mas;

import net.sourceforge.jswarm_pso.FitnessFunction;

import java.util.HashMap;

/**
 * Sample Fitness function
 * 		f( x1 , x2 ) = 1 - Sqrt( ( x1 - 1/2 )^2 + ( x2 - 1/2 )^2 )
 *
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 */
public class PSOFitnessFunction extends FitnessFunction {

    int quality;
    int quantity;
    int dim;
    HashMap<String, Object> supplyRest;
    double lon;
    double lat;
    int constraint;
    double[] distances;

    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    public PSOFitnessFunction(int quality, int quantity, int dim, HashMap<String, Object> supplyRest, int constraint, double[] distances) {
        super(false);                                   //minimize the function
        this.quality = quality;
        this.quantity = quantity;
        this.dim = dim;
        //this.lon = lon;
        //this.lat = lat;
        this.supplyRest = supplyRest;
        this.constraint = constraint;
        this.distances = distances;
    }


    /**
     * Evaluates a particles at a given position
     * @param position : Particle's position
     * @return Fitness function for a particle
     */
    public double evaluate(double position[]) {
        double count = 0;
        double obj = 0;
        //double obj_position;
        int aQuantity;
        int aQuality;
        int aConstraint;
        //double aLon;
        //double aLat;
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < this.dim*3; i++) {
            count += position[i];
        }
        if (count > (double)this.quantity) {                 //hohe Strafkosten für jede Einheit über Quantity
            //obj = (count + (double)this.quantity);
            //obj = (count + distances.length);
            obj = count;
            //obj = 1000;
        } else {                                        //hier eigentliche Berechnung der Fitness
            double distCost = 0;
            double qualCost = 0;
            double amount = 0;
            int edges = 0;
            for (int i = 0; i < this.dim*3; i++) {
                if (position[i] > 0) {

                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
                    /*
                    boolean supplyAlreadyUsed = false;
                    if (supplyRest.containsKey(Integer.toString(i))) {
                        aQuantity = (int) supplyRest.get(Integer.toString(i));
                        if (aQuantity != (int) angebot.get("quantity")) {
                            supplyAlreadyUsed = true;
                        }
                    } else {
                        aQuantity = (int) angebot.get("quantity");
                    }
                    aConstraint = (int)angebot.get("constraint");
                    double constraintsViolated = matching(aQuantity, aQuality, position[i], edges, aConstraint, supplyAlreadyUsed, distances[i]);

                     */
                    //if (constraintsViolated == 0) {
                        //aLon = (double) angebot.get("lon");
                        //aLat = (double) angebot.get("lat");
                        //if (position[i] > (double) aQuantity) {
                        //    obj += 1;
                        //} else {
                            double dist = this.distances[i];
                            double delta = Math.abs(this.quality - aQuality);
                            if (delta == 0) {
                                delta = 0.0;
                            } else if (delta == 1) {
                                delta = 0.5;
                            } else {
                                delta = 1.0;
                            }
                            distCost += dist * position[i];
                            qualCost += delta * position[i];
                            amount += position[i];
                            edges += 1;
                        //}
                    //} else {
                    //    //obj += 1;
                    //    obj += constraintsViolated;
                    //}
                }
            }
            if (amount > 0) {
                obj += 0.47 * (distCost / (140000 * this.quantity)) + 0.03 * (qualCost / this.quantity) + 0.5 * ((this.quantity - amount) / this.quantity);
            } else {
                obj += 0.5;
            }
        }
        return obj;
    }

    public boolean checkSolution(double[] position) {
        HashMap<String, Object> angebot = new HashMap();
        double count = 0;
        int aQuantity;
        int aQuality;
        int aConstraint;
        for (int i = 0; i < this.dim*3; i++) {
            count += position[i];
        }
        if (count > (double)this.quantity) {                 //hohe Strafkosten für jede Einheit über Quantity
            return false;
        } else {                                        //hier eigentliche Berechnung der Fitness
            /*
            int edges = 0;
            for (int i = 0; i < this.dim*3; i++) {
                if (position[i] > 0) {
                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
                    boolean supplyAlreadyUsed = false;
                    if (supplyRest.containsKey(Integer.toString(i))) {
                        aQuantity = (int) supplyRest.get(Integer.toString(i));
                        if (aQuantity != (int) angebot.get("quantity")) {
                            supplyAlreadyUsed = true;
                        }
                    } else {
                        aQuantity = (int) angebot.get("quantity");
                    }
                    aConstraint = (int)angebot.get("constraint");
                    if (matching(aQuantity, aQuality, position[i], edges, aConstraint, supplyAlreadyUsed, distances[i]) == 0) {
                        edges += 1;
                    } else {
                        return false;
                    }
                }
            }

             */
            return true;
        }
    }

    public double matching(int quantity, int quality, double demand, int edges, int constraint, boolean supplyAlreadyUsed, double dist) {

        double constraintsViolated = 0;
        if (Math.abs(this.quality - quality) > 2) { //bedeutet, erstmal grundsätzlich wählbar unabhängig von constraints
            constraintsViolated += 1;
        }
        if (quantity < demand) {
            constraintsViolated += 1;
        }
        //constraintsViolated += checkConstraints(quantity, quality, demand, edges, constraint, supplyAlreadyUsed, dist);
        return constraintsViolated;


/*
        //HashMap<String, Object> angebot = new HashMap();
        //angebot = (HashMap) Blackboard.get(Integer.toString(m));
        if (Math.abs(this.quality - quality) <= 2) { //bedeutet, erstmal grundsätzlich wählbar unabhängig von constraints
            if (quantity >= demand) {
                boolean notMatching = checkConstraints(quantity, quality, demand, edges, constraint, supplyAlreadyUsed);
                if (notMatching) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;*/
    }


    /*
    public double checkConstraints(int quantity, int quality, double demand, int edges, int constraint, boolean supplyAlreadyUsed, double dist) {
        boolean notMatching = false;
        //double constraintsViolated = 0;
        //int sRest;
        //double dRest;
        HashMap<String, Object> angebot = new HashMap();
        angebot = (HashMap)Blackboard.get(Integer.toString(rn));
        switch(this.constraint) {
            case 1:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                break;
            case 2:
                if (quantity < 50) {
                    notMatching = true;
                }
                break;
            case 3:
                if (quality != this.quality) {
                    notMatching = true;
                }
                break;
            case 4:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                if (quantity < 50) {
                    notMatching = true;
                }
                break;
            case 5:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                if (quality != this.quality) {
                    notMatching = true;
                }
                break;
            case 6:
                if (quantity < 50) {
                    notMatching = true;
                }
                if (quality != this.quality) {
                    notMatching = true;
                }
                break;
            case 7:
                if (dist > 20000.0) {
                    notMatching = true;
                }
                if (quantity < 50) {
                    notMatching = true;
                }
                if (quality != this.quality) {
                    notMatching = true;
                }
                break;
            case 8:
                break;
        }
        if (notMatching) {
            return notMatching;
        }
        switch(constraint) {
            case 1:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                break;
            case 2:
                if ((demand / (double)quantity) < 0.7) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                break;
            case 3:
                if (this.quality != quality) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                break;
            case 4:
                if (supplyAlreadyUsed) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                if ((demand / (double)quantity) < 0.7) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                break;
            case 5:
                if (supplyAlreadyUsed) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                if (this.quality != quality) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                break;
            case 6:
                if ((demand / (double)quantity) < 0.7) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                if (this.quality != quality) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                break;
            case 7:
                if (supplyAlreadyUsed) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                if ((demand / (double)quantity) < 0.7) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                if (this.quality != quality) {
                    notMatching = true;
                    constraintsViolated += 0.01;
                }
                break;
            case 8:
                break;
        }
        //return notMatching;
        return constraintsViolated;
    }

     */

}