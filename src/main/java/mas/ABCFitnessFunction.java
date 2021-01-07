package mas;

import java.util.HashMap;

public class ABCFitnessFunction {
    int quality;
    int quantity;
    int dim;
    HashMap<String, Object> supplyRest;
    int constraint;
    double[] distances;

    public ABCFitnessFunction(int quality, int quantity, int dim, HashMap<String, Object> supplyRest, int constraint, double[] distances) {

        this.quality = quality;
        this.quantity = quantity;
        this.dim = dim;
        this.supplyRest = supplyRest;
        this.constraint = constraint;
        this.distances = distances;
    }

    public double eval(double[] position) {

        double count = 0;
        double obj = 0;
        int aQuality;
        HashMap<String, Object> angebot = new HashMap();
        for (int i = 0; i < this.dim * 3; i++) {
            count += position[i];
        }
        if (count > (double) this.quantity) {                 //hohe Strafkosten für jede Einheit über Quantity
            obj = count;
        } else {                                        //hier eigentliche Berechnung der Fitness
            double distCost = 0;
            double qualCost = 0;
            double amount = 0;
            for (int i = 0; i < this.dim * 3; i++) {
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
