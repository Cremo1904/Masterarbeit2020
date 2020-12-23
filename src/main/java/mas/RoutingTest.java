package mas;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.*;
import com.graphhopper.config.*;
import com.graphhopper.*;
import com.graphhopper.reader.osm.GraphHopperOSM;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Locale;

public class RoutingTest {

    public static void main(String[] args) {

        GraphHopper hopper = new GraphHopperOSM().forServer();
        hopper.setDataReaderFile("niedersachsen-latest.osm.pbf");
        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/routing-graph-cache");
        hopper.setEncodingManager(EncodingManager.create("car"));

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
        // explicitly allow that the calling code can disable this speed mode
        hopper.getRouterConfig().setCHDisablingAllowed(true);

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        hopper.importOrLoad();

        double margin1 = 0.45;
        double grenze1 = 52.9;
        double margin2 = 0.7;
        double grenze2 = 7.8;

        MersenneTwister rng = new MersenneTwister();
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));
        System.out.println("Neue Koordinate: " + (margin1 * rng.nextDouble() + grenze1) + ", " + (margin2 * rng.nextDouble() + grenze2));

        //double[] orig = new double[]{53.1895450d, 8.1691740d};
        //double[] dest = new double[]{53.3972980d, 8.1491010d};
        double[] orig = new double[]{52.9907418d, 7.9069624d};
        double[] dest = new double[]{53.2131607d, 8.2230877d};

        GHRequest request = new GHRequest(orig[0], orig[1], dest[0], dest[1]);
        request.putHint("calcPoints", false);
        request.putHint("instructions", false);
        request.setProfile("car").setLocale(Locale.GERMANY);
        GHResponse route = hopper.route(request);
        if (route.hasErrors())
            throw new RuntimeException(route.getErrors().toString());

        ResponsePath path = route.getBest();

        System.out.println("Zeit: " + path.getTime());
        System.out.println("Distanz: " + path.getDistance());

        System.out.println("Helau");

    }


}