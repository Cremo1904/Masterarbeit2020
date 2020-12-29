package mas;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class MainDriver {

    public static void main(String[] args) {
        /*
        NQueenProblem Q = new NQueenProblem(8);
        HillClimbing HC = new HillClimbing();
        HC.solve(Q, HillClimbingStrategy.FIRST_CHOICE,false,5);
        */

        /*
        NQueenProblem Q = new NQueenProblem(8);
        SimulatedAnnealing SA = new SimulatedAnnealing();
        SA.solve(Q,SimulatedAnnealingStrategy.LINEAR_TEMPERATURE,false,100000);
        */

        int quality;
        int quantity;
        int index;
        double lon;
        double lat;
        int constraint;

        MiniMAS mas = new MiniMAS();
        //OptimizeAgent[] agents = new OptimizeAgent[m];
        mas.initHopper();
        double[] distances = new double[300];

        try {

            FileReader fr2 = new FileReader("Angebote_300C");
            BufferedReader br2 = new BufferedReader(fr2);

            for (int i = 0; i < (300); i++) {
                index = Integer.parseInt(br2.readLine());
                quality = Integer.parseInt(br2.readLine());
                lon = Double.parseDouble(br2.readLine());
                lat = Double.parseDouble(br2.readLine());
                quantity = Integer.parseInt(br2.readLine());
                constraint = Integer.parseInt(br2.readLine());

                HashMap<String, Object> angebot = new HashMap();
                angebot.put("quality", quality );
                angebot.put("quantity", quantity);
                angebot.put("lon", lon);
                angebot.put("lat", lat);
                angebot.put("constraint", constraint);

                Blackboard.put(Integer.toString(index), angebot);

            }


            HashMap<String, Object> angebot = new HashMap();
            for (int j = 0; j < 300; j++) {
                angebot = (HashMap) Blackboard.get(Integer.toString(j));
                GHRequest request = new GHRequest(53.3133432, 7.6189827, (double)angebot.get("lat"), (double)angebot.get("lon"));
                request.putHint("calcPoints", false);
                request.putHint("instructions", false);
                request.setProfile("car").setLocale(Locale.GERMANY);
                GHResponse route = mas.hopper.route(request);
                if (route.hasErrors())
                    throw new RuntimeException(route.getErrors().toString());
                ResponsePath path = route.getBest();
                distances[j] = path.getDistance();
            }

        } catch (IOException e) {

        }

        /*
        long time1 = System.currentTimeMillis();
        SAProblemTest prob = new SAProblemTest(100, 60, distances);
        SimulatedAnnealing SA = new SimulatedAnnealing(0.00015);
        SA.solve(prob,SimulatedAnnealingStrategy.EXPOTENTIAL,false);
        long time2 = System.currentTimeMillis();
        System.out.println("Zeit in ms: " + (time2-time1));

         */



        EvolutionaryProblem EP= new EvolutionaryProblem(100, 85, distances);
        EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(25, 10, 85);
        long time1 = System.currentTimeMillis();
        GA.solve(EP,5000);
        long time2 = System.currentTimeMillis();
        System.out.println("Zeit in ms: " + (time2-time1));
        System.out.println(GA.finalState.toString());
        System.out.println("Fitness : " + EP.fitness(GA.finalState));


    }

}
