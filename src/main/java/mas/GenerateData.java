package mas;

import org.apache.commons.math3.random.MersenneTwister;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateData {

    public static void main(String[] args) {

        int n = 1500;
        int count8 = 0;
        int count7 = 0;
        int count6 = 0;
        int count5 = 0;
        int count4 = 0;
        int count3 = 0;
        int count2 = 0;
        int count1 = 0;

        try {
            FileWriter fw = new FileWriter("Angebote_1500C");
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
            }

            bw.close();
            System.out.println("Constraint-Anteile: 1: " + count1 + ", 2: " + count2 + ", 3: " + count3 + ", 4: " + count4 + ", 5: " + count5 + ", 6: " + count6 + ", 7: " + count7 + ", 8: " + count8);

        } catch (IOException e) {

        }

    }
}
