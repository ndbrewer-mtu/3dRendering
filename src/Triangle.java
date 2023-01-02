package src;

import java.awt.Color;

/**
 * Triangle in given coordinates.<br>
 * <br>
 * Uses Vertex to create a point on the shape.<br>
 * <br>
 * <h2>Example Code</h2>
 * <code>new Triangle(new Vertex(100, 100, 100),
                      new Vertex(-100, -100, 100),
                      new Vertex(100, -100, -100),
                      Color.RED);</code>
 */
public class Triangle {
    Vertex v1;
    Vertex v2;
    Vertex v3;
    Color color;
    /**
     * <h2>creates a triangle in given space.</h2>
     * 
     * @param v1 First vertex of Triangle.
     * @param v2 Second vertex of Triangle.
     * @param v3 Third vertex of Triangle
     * @param color Color of Triangle.
     */
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }
}
