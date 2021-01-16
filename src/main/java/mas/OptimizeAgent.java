package mas;

import abc.ABCBehaviour;
import es.ESBehaviour;
import pso.PSOBehaviour;
import rs.RSBehaviour;
import sa.SABehaviour;

import java.util.*;

public class OptimizeAgent extends AbstractCOHDAAgent {

    private int dim;
    private int index;
    private int quality;
    private int quantity;
    private int constraint;
    private int algo;
    private double[] distances;
    private long counter = 0;
    private long positive = 0;
    private int limit = 0;


    public OptimizeAgent(String id, int index, int dim, int quality, int quantity, int constraint, int algo, double[] distances) {
        super(id);
        this.dim=dim;
        this.index=index;
        this.quality=quality;
        this.quantity=quantity;
        this.constraint=constraint;
        this.algo = algo;
        this.distances = distances;
    }

    public void init() {
        this.limit = 0;
        double tv = 0.5 * dim;
        createKappa(tv);
    }


    @Override
    protected WorkingMemory decide() {

        WorkingMemory kappa2 = kappa.copy();
        double targetValue = kappa2.getTargetValue();
        HashMap<String, Object> supplyRest = kappa2.getSupplyRest();
        HashMap<String, Object> myDemand = kappa2.getMyDemand();
        HashMap<String, Object> myEdges = kappa2.getMyEdges();
        HashMap<String, String> contribution = kappa2.getContribution();
        demand demandSpec;

        //hier aus demand holen, welche angebote genutzt wurden und diese zurücksetzen und auch demand
        if (myDemand.containsKey(getId())) {
            demandSpec = (demand) myDemand.get(getId());
            for (String str : demandSpec.getEdges()) {
                edges edgeSpec = (edges) myEdges.get(str);
                int add = edgeSpec.getQuantity();
                int supply = edgeSpec.getSupply();
                supplyRest.put(Integer.toString(supply), (int)supplyRest.get(Integer.toString(supply)) + add );
                myEdges.remove(str);
            }
            demandSpec = new demand(this.quantity, this.quantity, new HashSet<String>());
        } else {
            demandSpec = new demand(this.quantity, this.quantity, new HashSet<String>());
        }
        counter = counter +1;


        double[] vector = new double[dim*3];
        for (int i = 0; i < dim*3; i++) {
            vector[i] = 0;
        }
        switch(algo) {
            case 1:
                RSBehaviour rsbehaviour = new RSBehaviour(1);
                vector = rsbehaviour.generateSolution(this.quantity, this.quality, supplyRest, dim, this.constraint, this.distances);
                double calls = (double) Blackboard.get("calls");
                calls += 1;
                Blackboard.put("calls", calls);
                break;
            case 2:
                PSOBehaviour psobehaviour = new PSOBehaviour(2);
                vector = psobehaviour.generateSolution(this.quantity, this.quality, supplyRest, dim, this.constraint, this.distances);
                break;
            case 3:
                if (limit == 3) {
                    return kappa;
                }
                SABehaviour saBehaviour = new SABehaviour(3);
                vector = saBehaviour.generateSolution(this.quantity, this.quality, supplyRest, dim, this.constraint, this.distances);
                break;
            case 4:
                if (limit == 3) {
                    return kappa;
                }
                ESBehaviour esBehaviour = new ESBehaviour(4);
                vector = esBehaviour.generateSolution(this.quantity, this.quality, supplyRest, dim, this.constraint, this.distances);
                break;
            case 5:
                ABCBehaviour abcBehaviour = new ABCBehaviour(5);
                vector = abcBehaviour.generateSolution(this.quantity, this.quality, supplyRest, dim, this.constraint, this.distances);
                break;
        }
        for (int i = 0; i < dim*3; i++) {
            if (vector[i] > 0) {
                int quantity = (int)vector[i];
                HashMap<String, Object> angebot = new HashMap();
                angebot = (HashMap) Blackboard.get(Integer.toString(i));
                int einheitenAngebot = (int) angebot.get("quantity");
                if (supplyRest.containsKey(Integer.toString(i))) {
                    if (((int) supplyRest.get(Integer.toString(i)) - quantity) > 0) {
                        supplyRest.put(Integer.toString(i), ((int) supplyRest.get(Integer.toString(i)) - quantity));
                    } else {
                        supplyRest.put(Integer.toString(i), 0);
                    }
                } else {
                    if ((einheitenAngebot - quantity) > 0) {
                        supplyRest.put(Integer.toString(i), (einheitenAngebot - quantity));
                    } else {
                        supplyRest.put(Integer.toString(i), 0);
                    }
                }
                String newEdgeId = UUID.randomUUID().toString();
                double distance = this.distances[i];
                int delta = Math.abs(this.quality - (int) angebot.get("quality"));
                edges newEdge = new edges(i, getId(), quantity, delta, distance);
                myEdges.put(newEdgeId, newEdge);
                demandSpec.getEdges().add(newEdgeId);
                demandSpec.setRest(demandSpec.getRest() - quantity);
            }
        }
        myDemand.put(getId(), demandSpec);

        boolean success = false;
        double tv = objective(myDemand, myEdges);
        if (targetValue > tv) {
            kappa2.setTargetValue(tv);
            kappa2.setSupplyRest(supplyRest);
            kappa2.setMyDemand(myDemand);
            kappa2.setMyEdges(myEdges);

            if (!contribution.containsValue(getId())) {
                contribution.put(Integer.toString(contribution.size() + 1), getId());
            } else {
                for (int i = 1; i <= contribution.size(); i++) {
                    if (contribution.get(Integer.toString(i)) == getId()) {
                        contribution.remove(Integer.toString(i));
                        for (int j = i+1; j <= (contribution.size()+1); j++) {
                            String temp = contribution.get(Integer.toString(j));
                            contribution.remove(Integer.toString(j));
                            contribution.put(Integer.toString(j-1), temp);
                        }
                        contribution.put(Integer.toString(contribution.size()+1), getId());
                        break;
                    }
                }
            }
            kappa2.setContribution(contribution);
            positive++;
            success = true;
            limit = 0;
        } else {
            limit += 1;
        }

        if (success) {
            return kappa2;
        } else {
            return kappa;
        }
    }


    @Override
    public double objective(HashMap<String, Object> demandInput, HashMap<String, Object> edgesInput) {
        Set<String> keys = demandInput.keySet();
        double dist;
        double delta;
        double rest;
        double demand;
        int quantity;
        double tv = 0.0;
        double tv_local;
        double edgecostDist;
        double edgecostDistSum = 0;
        double edgecostQuality;
        double global = (double)Blackboard.get("global");
        int i = 0;
        double demand_sum = 0;
        for (String aid : keys) {
            demand demandSpec = (demand)demandInput.get(aid);
            demand = demandSpec.getQuantity();
            rest = demandSpec.getRest();
            edgecostDist = 0.0;
            edgecostQuality = 0.0;
            for (String str : demandSpec.getEdges()) {
                edges edgeSpec = (edges)edgesInput.get(str);
                dist = edgeSpec.getDistance();
                int j = edgeSpec.getDelta();
                if (j==0) {
                    delta = 0.0;
                } else if (j == 1) {
                    delta = 0.5;
                } else {
                    delta = 1.0;
                }
                quantity = edgeSpec.getQuantity();
                edgecostDist = edgecostDist + (double)quantity * dist;
                edgecostQuality = edgecostQuality + (double)quantity * delta;
            }
            tv_local = 0.47 * (edgecostDist / (140000 * global)) + 0.03 * (edgecostQuality / global); // + 0.5 * (rest/global);
            tv = tv + tv_local;
            demand_sum += (demand-rest);
            edgecostDistSum += edgecostDist;
            i++;
        }
        tv = tv + 0.5 * ((global - demand_sum)/global);
        Blackboard.put("Einheitenkilometer", (edgecostDistSum/(demand_sum))/1000);
        return tv;
    }


    public double getValue() {
        return kappa.getTargetValue();
    }

    public HashMap getMyDemand() {
        return kappa.getMyDemand();
    }

    public HashMap getMyEdges() {
        return kappa.getMyEdges();
    }

    public int getLambda() {
        return kappa.getLambda();
    }

    public long getCounter() {
        return this.counter;
    }

    public long getPositive() {
        return this.positive;
    }

}
