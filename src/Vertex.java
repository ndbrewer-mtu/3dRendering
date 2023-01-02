package src;

/**
 * Point in space.<br>
 * <br>
 * uses (x,y,z).
 * <h2>Example Code</h2>
 * <code>new Vertex(100.0, 100.0, 100.0);</code>
 */
public class Vertex {
    double x;
    double y;
    double z;

    /**
     * <h2>Creates a vertex point at given values.</h2>
     * 
     * @param x distance left to right.
     * @param y distance up and down.
     * @param z depth from camera. (positive means towards observer)
     */
    Vertex(double x,double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
