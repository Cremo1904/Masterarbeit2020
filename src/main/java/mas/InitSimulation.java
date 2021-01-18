package mas;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class InitSimulation {

    public static void main(String[] args) {


        // set up simulation
        int m=10;                       // Anzahl Agenten                       10; 50; 250
        double rep = 2.0;              // Anzahl Wiederholungen
        int algo = 1;                   // Algorithmus wählen,                  1: RS; 2: PSO; 3: SA; 4: ES; 5: ABC
        int c = 1;                      // Constraint-Verteilung                1: C1; 2: C2
        boolean readDist = true;        // Distanzen lesen oder berechnen       true: lesen; false: berechnen




        int quality;
        int quantity;
        int index;
        double lon;
        double lat;
        int constraint;
        MiniMAS mas = new MiniMAS();
        OptimizeAgent[] agents = new OptimizeAgent[m];
        mas.initHopper();
        double gesamtNachfrage = 0.0;
        String scenario = "";
        try {


            //set up file readers and writers
            if (m == 10) {
                scenario = "small";
            } else if (m == 50) {
                scenario = "medium";
            } else {
                scenario = "large";
            }
            if (c == 1) {
                scenario = scenario + "_c1";
            } else {
                scenario = scenario + "_c2";
            }
            FileReader fr = new FileReader("Nachfragen_" + scenario);
            BufferedReader br = new BufferedReader(fr);
            FileReader fr2 = new FileReader("Angebote_" + scenario);
            BufferedReader br2 = new BufferedReader(fr2);
            FileReader fr3 = new FileReader("Distances_" + scenario);
            BufferedReader br3 = new BufferedReader(fr3);
            FileWriter fw = new FileWriter("Distances_xxx");                    //output file needs to be specified if you want to calculate distances
            BufferedWriter bw = new BufferedWriter(fw);


            //put supply infos in blackboard
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


            //create agents with demand infos
            for (int i = 0; i < m; i++) {
                index = Integer.parseInt(br.readLine());
                quality = Integer.parseInt(br.readLine());
                lon = Double.parseDouble(br.readLine());
                lat = Double.parseDouble(br.readLine());
                quantity = Integer.parseInt(br.readLine());
                gesamtNachfrage += quantity;
                constraint = Integer.parseInt(br.readLine());
                System.out.println("Nachfrage: " + index + ",  Quali: " + quality + ",  Einheiten: " + quantity + ",  Position: " + lon + ", " + lat + ",Constraint: " + constraint);

                //get distances from file or calculate
                double[] distances = new double[m*3];
                HashMap<String, Object> angebot = new HashMap();
                for (int j = 0; j < m*3; j++) {
                    if (readDist) {
                        distances[j] = Double.parseDouble(br3.readLine());
                    } else {
                        angebot = (HashMap) Blackboard.get(Integer.toString(j));
                        System.out.println(angebot.get("lat") + ", " + angebot.get("lon"));
                        GHRequest request = new GHRequest(lat, lon, (double)angebot.get("lat"), (double)angebot.get("lon"));
                        request.putHint("calcPoints", false);
                        request.putHint("instructions", false);
                        request.setProfile("car").setLocale(Locale.GERMANY);
                        GHResponse route = mas.hopper.route(request);
                        if (route.hasErrors())
                            throw new RuntimeException(route.getErrors().toString());
                        ResponsePath path = route.getBest();
                        distances[j] = path.getDistance();
                        bw.write(distances[j] + "\n");
                    }
                }
                if (!readDist) {
                    bw.close();
                }

                //add agents to mas
                agents[i] = new OptimizeAgent(UUID.randomUUID().toString(), index, m, quality, quantity, constraint, algo, distances);
                mas.add(agents[i]);
            }


            //connect agents, create neighborhood
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
            System.out.println(e);
        }


        //variables for output evaluation
        double best = Double.MAX_VALUE;
        double worst = 0.0;
        double result = 0;
        long timeSum = 0;
        double sum = 0.0;
        long time = 0;
        long time2 = 0;
        double restNachfrage = 0.0;
        double befriedigtAnteil = 0.0;
        double unitKilometerAve = 0.0;
        double callsAve = 0.0;
        double ticSum = 0;
        int decides = 0;
        int successes = 0;
        int edgeCount = 0;
        Blackboard.put("global", gesamtNachfrage);
        Blackboard.put("calls", 0.0);


        //perform chosen number of repetitions of scenario
        for (int j = 0; j < rep; j++) {
            try {
                FileWriter fw = new FileWriter("Ausgabe_" + scenario + "_algo_" + algo + "_run_" + j + ".dat");
                BufferedWriter bw = new BufferedWriter(fw);

                //get and printout/persist runtime
                mas.sendMessage(new Message(agents[0].getId(), null, "start", null));
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

                //get result from some agent
                result = agents[0].getValue();
                for (int i = 0; i < m; i++) {
                    //System.out.println("Agent " + i + ": " + agents[i].getValue() + "  ; decide-Aufrufe: " + agents[i].getCounter() + "  ; erfolgreiche Schätzungen: " + agents[i].getPositive() + " ;  Lambda: " + agents[i].getLambda());
                    decides += agents[i].getCounter();
                    successes += agents[i].getPositive();
                }

                //get and printoz/persist tics
                System.out.println("Tics: " + mas.getRunden());
                bw.write("# Tics: " + mas.getRunden());
                bw.newLine();
                System.out.println("Endsumme: " + result);
                bw.write("# Endsumme: " + result);
                bw.newLine();
                System.out.println(mas.getSummen());
                bw.write(mas.getSummen());

                //get final state of network from some agent
                HashMap<String, Object> output = agents[0].getMyDemand();
                AbstractCOHDAAgent.demand demandSpec;
                System.out.println("\n");
                Set<String> keys = output.keySet();
                restNachfrage = gesamtNachfrage;
                double befriedigteNachfragen = 0.0;
                for (String str : keys) {
                    //bw.newLine();
                    //bw.write("Nachfrage " + str + ":");
                    demandSpec = (AbstractCOHDAAgent.demand) output.get(str);
                    //bw.write("  Rest: " + demandSpec.getRest());
                    restNachfrage -= demandSpec.getQuantity() - demandSpec.getRest();
                    if (demandSpec.getQuantity() != demandSpec.getRest()) {
                        befriedigteNachfragen ++;
                    }
                    //bw.write("  Menge: " + demandSpec.getQuantity());
                    //bw.write("  Kanten: " + demandSpec.getEdges().size());
                    edgeCount = edgeCount + demandSpec.getEdges().size();
                }
                bw.close();
                System.out.println("\nNachfragen befriedigt: " + befriedigteNachfragen + "  ;   Nachfragemenge befriedigt: " + (((gesamtNachfrage - restNachfrage)/gesamtNachfrage) * 100) + "%  ;  Kanten erzeugt: " + edgeCount);
                System.out.println("\nEndsumme: " + result + "\n\n");
            } catch (IOException e) {
                System.out.println(e);
            }

            //update best and worst results and further variables
            if (result < best) {best=result;}
            if (result > worst) {worst=result;}
            befriedigtAnteil += (((gesamtNachfrage - restNachfrage)/gesamtNachfrage) * 100);
            sum = sum + result;
            timeSum = timeSum + (time2-time);
            ticSum += (double)mas.getRunden();
            agents[0].objective(agents[0].getMyDemand(), agents[0].getMyEdges());
            unitKilometerAve += (double)Blackboard.get("Einheitenkilometer");
        }


        //persist or print out dimensions for evaluation
        try {
            FileWriter fw2 = new FileWriter("Ausgabe_" + scenario + "_algo_" + algo + "_results.dat");
            BufferedWriter bw2 = new BufferedWriter(fw2);

            double relativeSuccess = (double)successes / (double) decides;
            callsAve = (double)Blackboard.get("calls");
            System.out.println("Beste Ergebnis: " + best);
            bw2.write("Beste Ergebnis: " + best);
            bw2.newLine();
            System.out.println("Schlechtestes Ergebnis: " + worst);
            bw2.write("Schlechtestes Ergebnis: " + worst);
            bw2.newLine();
            System.out.println("Average Ergebnis: " + sum/rep);
            bw2.write("Average Ergebnis: " + sum/rep);
            bw2.newLine();
            System.out.println("Precision Error: " + ((worst-best)/(sum/rep))*100);
            bw2.write("Precision Error: " + ((worst-best)/(sum/rep))*100);
            bw2.newLine();
            System.out.println("Nachfragemenge befriedigt: " + befriedigtAnteil/rep + "%");
            bw2.write("Nachfragemenge befriedigt: " + befriedigtAnteil/rep + "%");
            bw2.newLine();
            System.out.println("Average Msg: " + ((double)mas.getMessageCount()-rep)/rep);
            bw2.write("Average Msg: " + ((double)mas.getMessageCount()-rep)/rep);
            bw2.newLine();
            System.out.println("Average Runden: " + (ticSum-rep)/rep);
            bw2.write("Average Runden: " + (ticSum-rep)/rep);
            bw2.newLine();
            System.out.println("Nachrichten pro Runde: " + ((double)mas.getMessageCount()/rep)/(ticSum/rep));
            bw2.write("Nachrichten pro Runde: " + ((double)mas.getMessageCount()/rep)/(ticSum/rep));
            bw2.newLine();
            System.out.println("Einheitenkilometer je befriedigte Einheit: " + unitKilometerAve/rep);
            bw2.write("Einheitenkilometer je befriedigte Einheit: " + unitKilometerAve/rep);
            bw2.newLine();
            System.out.println("Objective Aufrufe je Agent: " + (callsAve/m)/rep);
            bw2.write("Objective Aufrufe je Agent: " + (callsAve/m)/rep);
            bw2.newLine();
            System.out.println("Erfolgreiche Schätzungen: " + relativeSuccess*100 + "%   ; Anzahl: " + decides + "  ;   Erfolgreich: " + successes);
            bw2.write("Erfolgreiche Schätzungen: " + relativeSuccess*100 + "%   ; Anzahl: " + decides + "  ;   Erfolgreich: " + successes);
            bw2.newLine();
            System.out.println("Average Kanten: " + edgeCount/rep);
            bw2.write("Average Kanten: " + edgeCount/rep);
            bw2.newLine();
            System.out.println("Average Zeit: " + timeSum/rep);
            bw2.write("Average Zeit: " + timeSum/rep);
            bw2.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
