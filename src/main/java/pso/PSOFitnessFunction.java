package pso;

import mas.Blackboard;
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
        int aQuality;
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < this.dim*3; i++) {
            count += position[i];
        }
        if (count > (double)this.quantity) {                 //hohe Strafkosten für jede Einheit über Quantity
            obj = count;
        } else {                                        //hier eigentliche Berechnung der Fitness
            double distCost = 0;
            double qualCost = 0;
            double amount = 0;
            for (int i = 0; i < this.dim*3; i++) {
                if (position[i] > 0) {

                    angebot = (HashMap) Blackboard.get(Integer.toString(i));
                    aQuality = (int) angebot.get("quality");
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
        double count = 0;
        for (int i = 0; i < this.dim*3; i++) {
            count += position[i];
        }
        if (count > (double)this.quantity) {
            return false;
        } else {
            return true;
        }
    }

}