package pso;

import net.sourceforge.jswarm_pso.Particle;

/**
 * sample particle
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *
 * modified by
 * @author Lukas Cremers
 */
public class PSOParticle extends Particle {

    public static int DIMENSION = 2;

    public PSOParticle() {
        super(DIMENSION); // Create a n-dimensional particle
    }

}

