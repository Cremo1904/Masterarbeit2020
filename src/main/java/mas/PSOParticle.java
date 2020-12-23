package mas;

import net.sourceforge.jswarm_pso.Particle;

/**
 * Simple particle example
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 */
public class PSOParticle extends Particle {

    public static int DIMENSION = 2;

    /** Default constructor */
    public PSOParticle() {
        super(DIMENSION); // Create a n-dimensional particle
    }



}

