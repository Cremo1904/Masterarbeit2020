package mas;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import org.apache.commons.math3.random.MersenneTwister;
import java.util.*;
import net.sourceforge.jswarm_pso.*;

public class PSOAgent extends AbstractCOHDAAgent {

    private int dim;
    private int index;
    private int quality;
    private int quantity;
    private int constraint;
    private double lon;
    private double lat;
    private long counter = 0;
    private long positive = 0;


    public PSOAgent(String id, int index, int dim, int quality, int quantity, double lon, double lat, int constraint) {
        super(id);
        this.dim=dim;
        this.index=index;
        this.quality=quality;
        this.quantity=quantity;
        this.lon=lon;
        this.lat=lat;
        this.constraint=constraint;
    }

    public void init() {
        double tv = 0.5 * dim;
        createKappa(tv);

        //initSwarm();
    }

    /*public void initSwarm() {

        // Create a swarm (using 'MyParticle' as sample particle and 'MyFitnessFunction' as fitness function)
        Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES, new PSOParticle(dim*3), new PSOFitnessFunction(this.quality, this.quantity, this.lon, this.lat, this.constraint, this));

        // Use neighborhood
        Neighborhood neigh = new Neighborhood1D(Swarm.DEFAULT_NUMBER_OF_PARTICLES / 5, true);
        swarm.setNeighborhood(neigh);
        swarm.setNeighborhoodIncrement(0.9);

        // Set position (and velocity) constraints. I.e.: where to look for solutions
        swarm.setInertia(0.95);
        swarm.setMaxPosition(1);
        swarm.setMinPosition(0);
        swarm.setMaxMinVelocity(0.1);

        int numberOfIterations = 100;
        for (int i = 0; i < numberOfIterations; i++) {
            swarm.evolve();
        }
        // Print results
		System.out.println(swarm.toStringStats());
		System.out.println("End: Example 1");
		swarm.getBestPosition();
    }*/

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

        MersenneTwister rng = new MersenneTwister();
        while (demandSpec.getRest() > 0) {
            int restNachfrage = (int)demandSpec.getRest();
            ArrayList<String> list = new ArrayList<String>();
            boolean success = false;
            int m = -1;
            int i = 0;
            while (i<(dim*3)) {
                while (true) {
                    m = rng.nextInt(dim * 3);
                    if (!list.contains(Integer.toString(m))) {
                        break;
                    }
                }
                if (matching(demandSpec, supplyRest, m)) {
                    success = true;
                    break;
                }
                list.add(Integer.toString(m));
                i++;
            }

            if (success && m != -1) {
                HashMap<String, Object> angebot = new HashMap();
                angebot = (HashMap) Blackboard.get(Integer.toString(m));
                int einheitenAngebot = (int) angebot.get("quantity");
                //int restNachfrage;
                if (supplyRest.containsKey(Integer.toString(m))) {
                    if (((int) supplyRest.get(Integer.toString(m)) - restNachfrage) > 0) {
                        supplyRest.put(Integer.toString(m), ((int) supplyRest.get(Integer.toString(m)) - restNachfrage));
                        restNachfrage = 0;
                    } else {
                        restNachfrage = restNachfrage - (int) supplyRest.get(Integer.toString(m));
                        supplyRest.put(Integer.toString(m), 0);
                    }
                } else {
                    if ((einheitenAngebot - restNachfrage) > 0) {
                        supplyRest.put(Integer.toString(m), (einheitenAngebot - restNachfrage));
                        restNachfrage = 0;
                    } else {
                        supplyRest.put(Integer.toString(m), 0);
                        restNachfrage = restNachfrage - einheitenAngebot;
                    }
                }

                String newEdgeId = UUID.randomUUID().toString();
                double distance = calcRoute(this.lat, this.lon, (double) angebot.get("lat"), (double) angebot.get("lon"));
                int delta = Math.abs(this.quality - (int) angebot.get("quality"));
                int quantity = (int)demandSpec.getRest() - restNachfrage;
                edges newEdge = new edges(m, getId(), quantity, delta, distance);
                myEdges.put(newEdgeId, newEdge);
                demandSpec.getEdges().add(newEdgeId);
                demandSpec.setRest((double)restNachfrage);
            } else {
                break;
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
            contribution.put(Integer.toString(contribution.size()+1), getId());
            kappa2.setContribution(contribution);
            positive++;
            success = true;
        }

        if (success) {
            return kappa2;
        } else {
            return kappa;
        }
    }

    public double calcRoute(double lat1, double lon1, double lat2, double lon2) {
        GHRequest request = new GHRequest(lat1, lon1, lat2, lon2);
        request.putHint("calcPoints", false);
        request.putHint("instructions", false);
        request.setProfile("car").setLocale(Locale.GERMANY);
        GHResponse route = getMAS().hopper.route(request);
        if (route.hasErrors())
            throw new RuntimeException(route.getErrors().toString());

        ResponsePath path = route.getBest();
        return path.getDistance();
    }


    public boolean matching(demand demandSpec, HashMap<String, Object> supplyRest, int m) {

        boolean success = false;
        HashMap<String, Object> angebot = new HashMap();
        angebot = (HashMap) Blackboard.get(Integer.toString(m));
        if (Math.abs(this.quality - (int) angebot.get("quality")) <= 2) { //bedeutet, erstmal grundsätzlich wählbar unabhängig von constraints
            boolean notMatching = checkConstraints(demandSpec, supplyRest, m);
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


    public boolean checkConstraints(demand demandSpec, HashMap<String, Object> supplyRest, int rn) {
        boolean notMatching = false;
        int sRest;
        double dRest;
        HashMap<String, Object> angebot = new HashMap();
        angebot = (HashMap)Blackboard.get(Integer.toString(rn));
        switch(this.constraint) {
            case 1:
                if (demandSpec.getEdges().size() > 0) {
                    notMatching = true;
                }
                break;
            case 2:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                }
                break;
            case 3:
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 4:
                if (demandSpec.getEdges().size() > 0) {
                    notMatching = true;
                }
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                }
                break;
            case 5:
                if (demandSpec.getEdges().size() > 0) {
                    notMatching = true;
                }
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 6:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                }
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 7:
                if (demandSpec.getEdges().size() > 0) {
                    notMatching = true;
                }
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if (((double)sRest / dRest) < 0.7) {
                        notMatching = true;
                    }
                }
                if (this.quality != (int)angebot.get("quality")) {
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
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                }
                break;
            case 3:
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 4:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                }
                break;
            case 5:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 6:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                }
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 7:
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    if ((int)supplyRest.get(Integer.toString(rn)) != (int)angebot.get("quantity")) {
                        notMatching = true;
                    }
                }
                if (supplyRest.containsKey(Integer.toString(rn))) {
                    sRest = (int)supplyRest.get(Integer.toString(rn));
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                } else {
                    sRest = (int)angebot.get("quantity");
                    dRest = demandSpec.getRest();
                    if ((dRest / (double)sRest) < 0.7) {
                        notMatching = true;
                    }
                }
                if (this.quality != (int)angebot.get("quality")) {
                    notMatching = true;
                }
                break;
            case 8:
                break;
        }
        return notMatching;
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
        double edgecostQuality;

        int i = 0;
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
            tv_local = 0.47 * (edgecostDist / (140000 * demand)) + 0.03 * (edgecostQuality / demand) + 0.5 * (rest/demand);
            tv = tv + tv_local;
            i++;
        }
        tv = tv + ((dim-i) * 0.5);
        return tv;
    }


    public double getValue() {
        return kappa.getTargetValue();
    }

    public HashMap getMyDemand() {
        return kappa.getMyDemand();
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