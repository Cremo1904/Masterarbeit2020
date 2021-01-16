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

        int m=250;
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

        try {
            FileReader fr = new FileReader("Nachfragen_large_c2");
            BufferedReader br = new BufferedReader(fr);

            FileReader fr2 = new FileReader("Angebote_large_c2");
            BufferedReader br2 = new BufferedReader(fr2);

            FileReader fr3= new FileReader("Distances_large_c2");
            BufferedReader br3 = new BufferedReader(fr3);

            //FileWriter fw = new FileWriter("Distances_small_c2");
            //BufferedWriter bw = new BufferedWriter(fw);

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
                gesamtNachfrage += quantity;
                constraint = Integer.parseInt(br.readLine());
                System.out.println("Nachfrage: " + index + ",  Quali: " + quality + ",  Einheiten: " + quantity + ",  Position: " + lon + ", " + lat + ",Constraint: " + constraint);

                double[] distances = new double[m*3];
                HashMap<String, Object> angebot = new HashMap();
                for (int j = 0; j < m*3; j++) {
                    //angebot = (HashMap) Blackboard.get(Integer.toString(j));
                    //System.out.println(angebot.get("lat") + ", " + angebot.get("lon"));
                    //GHRequest request = new GHRequest(lat, lon, (double)angebot.get("lat"), (double)angebot.get("lon"));
                    //request.putHint("calcPoints", false);
                    //request.putHint("instructions", false);
                    //request.setProfile("car").setLocale(Locale.GERMANY);
                    //GHResponse route = mas.hopper.route(request);
                    //if (route.hasErrors())
                    //    throw new RuntimeException(route.getErrors().toString());
                    //ResponsePath path = route.getBest();
                    //distances[j] = path.getDistance();
                    distances[j] = Double.parseDouble(br3.readLine());
                    //bw.write(distances[j] + "\n");
                }

                agents[i] = new OptimizeAgent(UUID.randomUUID().toString(), index, m, quality, quantity, constraint, 2, distances);
                mas.add(agents[i]);
            }

            //bw.close();
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

        double rep = 1.0;                                                                       //hier anzahl wiederholungen eingeben
        double best = 100.0;
        double worst = 0.0;
        //double summe = 0;
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
        for (int j = 0; j < rep; j++) {
            try {
                FileWriter fw = new FileWriter("ausgabe.dat");
                BufferedWriter bw = new BufferedWriter(fw);

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

                result = agents[0].getValue();

                for (int i = 0; i < m; i++) {
                    //System.out.println("Agent " + i + ": " + agents[i].getValue() + "  ; decide-Aufrufe: " + agents[i].getCounter() + "  ; erfolgreiche Schätzungen: " + agents[i].getPositive() + " ;  Lambda: " + agents[i].getLambda());
                    decides += agents[i].getCounter();
                    successes += agents[i].getPositive();
                }


                System.out.println("Tics: " + mas.getRunden());
                bw.write("# Tics: " + mas.getRunden());
                bw.newLine();
                System.out.println("Endsumme: " + result);
                bw.write("# Endsumme: " + result);
                bw.newLine();
                System.out.println(mas.getSummen());
                bw.write(mas.getSummen());

                HashMap<String, Object> output = agents[0].getMyDemand();
                AbstractCOHDAAgent.demand demandSpec;
                System.out.println("\n");
                Set<String> keys = output.keySet();
                restNachfrage = gesamtNachfrage;
                double befriedigteNachfragen = 0.0;

                for (String str : keys) {
                    //System.out.println("\nNachfrage " + str + ":");
                    bw.newLine();
                    bw.write("Nachfrage " + str + ":");
                    demandSpec = (AbstractCOHDAAgent.demand) output.get(str);
                    //System.out.println("Rest: " + demandSpec.getRest());
                    bw.write("  Rest: " + demandSpec.getRest());
                    restNachfrage -= demandSpec.getQuantity() - demandSpec.getRest();
                    if (demandSpec.getQuantity() != demandSpec.getRest()) {
                        befriedigteNachfragen ++;
                    }
                    //System.out.println("Menge: " + demandSpec.getQuantity());
                    bw.write("  Menge: " + demandSpec.getQuantity());
                    //System.out.println("Kanten: " + demandSpec.getEdges().size());
                    bw.write("  Kanten: " + demandSpec.getEdges().size());
                    edgeCount = edgeCount + demandSpec.getEdges().size();
                }
                bw.close();
                System.out.println("\nNachfragen befriedigt: " + befriedigteNachfragen + "  ;   Nachfragemenge befriedigt: " + (((gesamtNachfrage - restNachfrage)/gesamtNachfrage) * 100) + "%  ;  Kanten erzeugt: " + edgeCount);
                System.out.println("\nEndsumme: " + result + "\n\n");
            } catch (IOException e) {
                System.out.println(e);
            }
            if (result < best) {best=result;}
            if (result > worst) {worst=result;}
            befriedigtAnteil += (((gesamtNachfrage - restNachfrage)/gesamtNachfrage) * 100);
            sum = sum + result;
            timeSum = timeSum + (time2-time);
            ticSum += (double)mas.getRunden();
            agents[0].objective(agents[0].getMyDemand(), agents[0].getMyEdges());
            unitKilometerAve += (double)Blackboard.get("Einheitenkilometer");
        }
        double relativeSuccess = (double)successes / (double) decides;
        callsAve = (double)Blackboard.get("calls");
        System.out.println("Beste Ergebnis: " + best);
        System.out.println("Schlechtestes Ergebnis: " + worst);
        System.out.println("Average Ergebnis: " + sum/rep);
        System.out.println("Precision Error: " + ((worst-best)/(sum/rep))*100);
        System.out.println("Nachfragemenge befriedigt: " + befriedigtAnteil/rep + "%");
        System.out.println("Average Msg: " + ((double)mas.getMessageCount()-rep)/rep);
        System.out.println("Average Runden: " + (ticSum-rep)/rep);
        System.out.println("Nachrichten pro Runde: " + ((double)mas.getMessageCount()/rep)/(ticSum/rep));
        System.out.println("Einheitenkilometer je befriedigte Einheit: " + unitKilometerAve/rep);
        System.out.println("Objective Aufrufe je Agent: " + (callsAve/m)/rep);
        System.out.println("Erfolgreiche Schätzungen: " + relativeSuccess*100 + "%   ; Anzahl: " + decides + "  ;   Erfolgreich: " + successes);
        System.out.println("Average Kanten: " + edgeCount/rep);
        System.out.println("Average Zeit: " + timeSum/rep);
    }

}
