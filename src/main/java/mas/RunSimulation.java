package mas;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class RunSimulation {

    //hier festlegen, welcher Agent genutzt wird
    //hier festlegen, welches szenario genutzt wird


    public static void main(String[] args) {

        int m=10;
        int quality;
        int quantity;
        int index;
        double lon;
        double lat;
        int constraint;


        MiniMAS mas = new MiniMAS();
        OptimizeAgent[] agents = new OptimizeAgent[m];
        mas.initHopper();

        try {
            FileReader fr = new FileReader("Nachfragen_10");
            BufferedReader br = new BufferedReader(fr);

            FileReader fr2 = new FileReader("Angebote_30");
            BufferedReader br2 = new BufferedReader(fr2);

            for (int i = 0; i < (m*3); i++) {
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
            for (int i = 0; i < m; i++) {
                index = Integer.parseInt(br.readLine());
                quality = Integer.parseInt(br.readLine());
                lon = Double.parseDouble(br.readLine());
                lat = Double.parseDouble(br.readLine());
                quantity = Integer.parseInt(br.readLine());
                constraint = Integer.parseInt(br.readLine());
                System.out.println("Nachfrage: " + index + ",  Quali: " + quality + ",  Einheiten: " + quantity + ",  Position: " + lon + ", " + lat + ",Constraint: " + constraint);

                double[] distances = new double[m*3];
                HashMap<String, Object> angebot = new HashMap();
                for (int j = 0; j < m*3; j++) {
                    angebot = (HashMap) Blackboard.get(Integer.toString(j));
                    GHRequest request = new GHRequest(lat, lon, (double)angebot.get("lat"), (double)angebot.get("lon"));
                    request.putHint("calcPoints", false);
                    request.putHint("instructions", false);
                    request.setProfile("car").setLocale(Locale.GERMANY);
                    GHResponse route = mas.hopper.route(request);
                    if (route.hasErrors())
                        throw new RuntimeException(route.getErrors().toString());
                    ResponsePath path = route.getBest();
                    distances[j] = path.getDistance();
                }

                agents[i] = new OptimizeAgent(UUID.randomUUID().toString(), index, m, quality, quantity, lon, lat, constraint, 2, distances);
                mas.add(agents[i]);
            }

            int j;
            for (int i=0;i<m;i++){
                j = i+1;
                if (i == m-1) {
                    j = 0;
                }
                System.out.println("Verbinde Agent " + i + " mit Agent " + j);
                mas.connect(agents[i], agents[j]);
            }
        } catch (IOException e) {

        }

        double best = 100.0;
        double summe = 0;
        long timeSum = 0;
        double sumSum = 0.0;
        long time = 0;
        long time2 = 0;

        for (int j = 0; j < 1000; j++) {
            try {
                FileWriter fw = new FileWriter("ausgabe.dat");
                BufferedWriter bw = new BufferedWriter(fw);

                mas.sendMessage(new Message(agents[0].getId(), null, "start", null));
                //mas.sendMessage(new Message(agents[0].getId(), "system", "start", null));

                time = System.currentTimeMillis();

                mas.execute();

                time2 = System.currentTimeMillis();
                System.out.println("Zeit vorher: " + time);
                bw.write("# Zeit vorher: " + time);
                bw.newLine();
                System.out.println("Zeit nachher: " + time2);
                bw.write("# Zeit nachher: " + time2);
                bw.newLine();

                System.out.println("Zeit Differenz: " + (time2 - time));
                bw.write("# Zeit Differenz: " + (time2 - time));
                bw.newLine();

                bw.write("# Anzahl Agenten: " + m);
                bw.newLine();

                summe = 0.0;
                for (int i = 0; i < m; i++) {
                    summe = summe + agents[i].getValue();
                    //System.out.println("Agent " + i + ": " + agents[i].getValue() + "  ; decide-Aufrufe: " + agents[i].getCounter() + "  ; erfolgreiche Schätzungen: " + agents[i].getPositive() + " ;  Lambda: " + agents[i].getLambda());
                }

                System.out.println("Tics: " + mas.getRunden());
                bw.write("# Tics: " + mas.getRunden());
                bw.newLine();

                System.out.println("Endsumme: " + summe);
                bw.write("# Endsumme: " + summe);
                bw.newLine();

                System.out.println(mas.getSummen());
                bw.write(mas.getSummen());

                HashMap<String, Object> output = agents[0].getMyDemand();
                AbstractCOHDAAgent.demand demandSpec;
                System.out.println("\n");
                Set<String> keys = output.keySet();
                double restNachfrage = 0.0;
                double gesamtNachfrage = 0.0;
                int edgeCount = 0;
                for (String str : keys) {
                    //System.out.println("\nNachfrage " + str + ":");
                    bw.newLine();
                    bw.write("Nachfrage " + str + ":");
                    demandSpec = (AbstractCOHDAAgent.demand) output.get(str);
                    //System.out.println("Rest: " + demandSpec.getRest());
                    bw.write("Rest: " + demandSpec.getRest());
                    restNachfrage = restNachfrage + demandSpec.getRest();
                    gesamtNachfrage = gesamtNachfrage + demandSpec.getQuantity();
                    //System.out.println("Menge: " + demandSpec.getQuantity());
                    bw.write("Menge: " + demandSpec.getQuantity());
                    //System.out.println("Kanten: " + demandSpec.getEdges().size());
                    bw.write("Kanten: " + demandSpec.getEdges().size());
                    edgeCount = edgeCount + demandSpec.getEdges().size();
                }
                bw.close();
                System.out.println("\nNachfrage befriedigt: " + (((gesamtNachfrage - restNachfrage)/gesamtNachfrage) * 100) + "%  ;  Kanten erzeugt: " + edgeCount);
                System.out.println("\nEndsumme: " + summe/m + "\n\n");
            } catch (IOException e) {
                System.out.println(e);
            }
            if (summe/m < best) {best=summe/m;}
            sumSum = sumSum + (summe/m);
            timeSum = timeSum + (time2-time);
        }
        System.out.println("Beste Ergebnis: " + best);
        System.out.println("Average Ergebnis: " + sumSum/100.0);
        System.out.println("Average Zeit: " + timeSum/100.0);
    }

}
