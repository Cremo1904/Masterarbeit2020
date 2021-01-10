package mas;

import java.util.HashMap;

public abstract class Behaviour {

    private int id;

    protected Behaviour(int id) {
        this.id=id;
    }

    protected abstract double[] generateSolution(int demand, int quality, HashMap<String, Object> supplyRest, int dim, int constraint, double[] distances);
}
