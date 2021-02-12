package mas;

import java.io.*;
import java.util.HashMap;

/**
 * Main class for generating datasets representing demand and supply
 * @author Lukas Cremers
 */
public class GenerateData {

    public static void main(String[] args) {

        // Generating Data
        /*
        int n = 50;
        int count8 = 0;
        int count7 = 0;
        int count6 = 0;
        int count5 = 0;
        int count4 = 0;
        int count3 = 0;
        int count2 = 0;
        int count1 = 0;

        try {
            FileWriter fw = new FileWriter("Nachfragen_medium_c2ttsdsadsa");
            BufferedWriter bw = new BufferedWriter(fw);

            int q = 1;
            int quo = n/6;
            int counter = 0;
            for (int i = 0; i < n; i++) {

                bw.write(i + "\n");
                bw.write(q + "\n");
                counter = counter + 1;
                if (counter >= quo){
                    counter = 0;
                    q = q + 1;
                    if (q>6) {q=1; quo=1;}
                }

                MersenneTwister rng = new MersenneTwister();
                double l = rng.nextDouble();
                double b = rng.nextDouble();
                l = (1.03 * l + 7.43) * 10000000;
                l = Math.round(l);
                l = l / 10000000;
                b = (0.61 * b + 52.78) * 10000000;
                b = Math.round(b);
                b = b / 10000000;
                bw.write(l + "\n");
                bw.write(b + "\n");

                bw.write(Math.round(99 * rng.nextDouble() + 1) + "\n");
                double constraint = rng.nextDouble();


                //c1
                if (constraint >= 0.7) {
                    bw.write(8 + "\n");
                    count8++;
                } else if(constraint >= 0.6) {
                    bw.write(7 + "\n");
                    count7++;
                } else if(constraint >= 0.5) {
                    bw.write(6 + "\n");
                    count6++;
                } else if(constraint >= 0.4) {
                    bw.write(5 + "\n");
                    count5++;
                } else if(constraint >= 0.3) {
                    bw.write(4 + "\n");
                    count4++;
                } else if(constraint >= 0.2) {
                    bw.write(3 + "\n");
                    count3++;
                } else if(constraint >= 0.1) {
                    bw.write(2 + "\n");
                    count2++;
                } else {
                    bw.write(1 + "\n");
                    count1++;
                }




                //c2
                if (constraint >= 0.8) {
                    bw.write(7 + "\n");
                    count7++;
                } else if(constraint >= 0.6) {
                    bw.write(6 + "\n");
                    count6++;
                } else if(constraint >= 0.4) {
                    bw.write(5 + "\n");
                    count5++;
                } else if(constraint >= 0.2) {
                    bw.write(4 + "\n");
                    count4++;
                } else {
                    bw.write(8 + "\n");
                    count8++;
                }



                //c3
                if (constraint >= 0.8) {
                    bw.write(1 + "\n");
                    count1++;
                } else if(constraint >= 0.6) {
                    bw.write(2 + "\n");
                    count2++;
                } else if(constraint >= 0.4) {
                    bw.write(3 + "\n");
                    count3++;
                } else {
                    bw.write(8 + "\n");
                    count8++;
                }



            }

            bw.close();
            System.out.println("Constraint-Anteile: 1: " + count1 + ", 2: " + count2 + ", 3: " + count3 + ", 4: " + count4 + ", 5: " + count5 + ", 6: " + count6 + ", 7: " + count7 + ", 8: " + count8);

        } catch (IOException e) {

        }
*/







        //Sorting Data

        try {
            FileReader fr = new FileReader("Nachfragen_medium_c2tt");
            BufferedReader br = new BufferedReader(fr);
            HashMap<Integer, Object> c1 = new HashMap();
            HashMap<Integer, Object> c2 = new HashMap();
            HashMap<Integer, Object> c3 = new HashMap();
            HashMap<Integer, Object> c4 = new HashMap();
            HashMap<Integer, Object> c5 = new HashMap();
            HashMap<Integer, Object> c6 = new HashMap();
            HashMap<Integer, Object> c7 = new HashMap();
            HashMap<Integer, Object> c8 = new HashMap();


            for (int i = 0; i < 50; i++) {                      //hier größe des datensatzes eingeben
                double[] array = new double[6];
                array[0] = Double.parseDouble(br.readLine());
                array[1] = Double.parseDouble(br.readLine());
                array[2] = Double.parseDouble(br.readLine());
                array[3] = Double.parseDouble(br.readLine());
                array[4] = Double.parseDouble(br.readLine());
                array[5] = Double.parseDouble(br.readLine());
                int j;
                switch((int)array[5]) {
                    case 1:
                        j = c1.size();
                        c1.put(j, array);
                        break;
                    case 2:
                        j = c2.size();
                        c2.put(j, array);
                        break;
                    case 3:
                        j = c3.size();
                        c3.put(j, array);
                        break;
                    case 4:
                        j = c4.size();
                        c4.put(j, array);
                        break;
                    case 5:
                        j = c5.size();
                        c5.put(j, array);
                        break;
                    case 6:
                        j = c6.size();
                        c6.put(j, array);
                        break;
                    case 7:
                        j = c7.size();
                        c7.put(j, array);
                        break;
                    case 8:
                        j = c8.size();
                        c8.put(j, array);
                        break;
                }
            }
            FileWriter fw = new FileWriter("Nachfragen_medium_c2t");
            BufferedWriter bw = new BufferedWriter(fw);

            double[] myDemand;
            for (int j = 0; j < c7.size(); j++) {
                myDemand = (double[])c7.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c6.size(); j++) {
                myDemand = (double[])c6.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c5.size(); j++) {
                myDemand = (double[])c5.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c4.size(); j++) {
                myDemand = (double[])c4.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c3.size(); j++) {
                myDemand = (double[])c3.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c2.size(); j++) {
                myDemand = (double[])c2.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c1.size(); j++) {
                myDemand = (double[])c1.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            for (int j = 0; j < c8.size(); j++) {
                myDemand = (double[])c8.get(j);
                bw.write((int)myDemand[0] + "\n");
                bw.write((int)myDemand[1] + "\n");
                bw.write(myDemand[2] + "\n");
                bw.write(myDemand[3] + "\n");
                bw.write((int)myDemand[4] + "\n");
                bw.write((int)myDemand[5] + "\n");
            }
            bw.close();


        } catch (IOException e) {

        }



    }
}
