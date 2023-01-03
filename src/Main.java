package src;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Main {

    public enum Shape {Tetrahedron, Cube, Sphere}

    static boolean wireframe = true;

    static Shape currentShape = Shape.Tetrahedron;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider for horizontal rotation.
        JSlider headingSlider = new JSlider(0,360,180); // from 0° to 360° starting at 180°.
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider for vertical rotation.
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL,-90,90,0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // Panel for buttons.
        JPanel buttonPanel = new JPanel(new GridLayout(0,4,2,0));
        buttonPanel.setBackground(Color.black);

        // The button to change the shape to Tetrahedron.
        JButton TetrahedronButton = new JButton("Tetrahedron");
        buttonPanel.add(TetrahedronButton);

        // The button to change the shape to Sphere Approximation.
        JButton sphereButton = new JButton("Sphere");
        buttonPanel.add(sphereButton);

        // The button to change the shape to Cube.
        JButton cubeButton = new JButton("Cube");
        buttonPanel.add(cubeButton);

        // The button to change the rendering mode to solid or back to wireframe.
        JButton wireframeButton = new JButton("Solid");
        buttonPanel.add(wireframeButton);

        pane.add(buttonPanel,BorderLayout.NORTH);

        // panel to display rendering result.
        JPanel renderPanel = new JPanel(){
            public void paintComponent(Graphics g){
                
                ArrayList<Triangle> tris;

                if(currentShape == Shape.Tetrahedron)
                    tris = createTetrahedronList();
                else if(currentShape == Shape.Cube)
                    tris = createCubeList();
                else if (currentShape == Shape.Sphere)
                    tris = createSphereList();
                else
                    tris = createEmptyList();

                double heading = Math.toRadians(headingSlider.getValue());
                Matrix3 transform = new Matrix3(new double[] {
                    Math.cos(heading), 0, Math.sin(heading),
                    0, 1, 0,
                    -Math.sin(heading), 0, Math.cos(heading)
                });

                double pitch = Math.toRadians(pitchSlider.getValue());
                transform = new Matrix3(new double[] {
                    1, 0, 0,
                    0, Math.cos(pitch), -Math.sin(pitch),
                    0, Math.sin(pitch), Math.cos(pitch)
                }).multiply(transform);
                
                if(wireframe)
                    renderWireframeShape(g, this, transform, tris);
                else
                    renderSolidShape(g, this, transform, tris);

            }
            private static void renderWireframeShape(Graphics g, Object comp, Matrix3 transform, ArrayList<Triangle> tris){

                JComponent component = (JComponent) comp;

                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.black);
                g2.fillRect(0, 0, component.getWidth(), component.getHeight());

                g2.translate(component.getWidth()/2, component.getHeight()/2);
                g2.setColor(Color.WHITE);
        
                for(Triangle t : tris){
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
        
                }
            }
            private static void renderSolidShape(Graphics g, Object comp, Matrix3 transform, ArrayList<Triangle> tris){
                JComponent component = (JComponent) comp;

                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.black);
                g2.fillRect(0, 0, component.getWidth(), component.getHeight());

                BufferedImage img = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);

                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                for(Triangle t : tris){

                    //have to manually translate since the image is not a Graphics2D object.
                    Vertex v1 = transform.transform(t.v1);
                    v1.x += component.getWidth() / 2;
                    v1.y += component.getHeight() / 2;
                    Vertex v2 = transform.transform(t.v2);
                    v2.x += component.getWidth() / 2;
                    v2.y += component.getHeight() / 2;
                    Vertex v3 = transform.transform(t.v3);
                    v3.x += component.getWidth() / 2;
                    v3.y += component.getHeight() / 2;

                    // normalize vertices to screen coordinates.
                    Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
                    Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
                    Vertex norm = new Vertex(
                         ab.y * ac.z - ab.z * ac.y,
                         ab.z * ac.x - ab.x * ac.z,
                         ab.x * ac.y - ab.y * ac.x
                    );
                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    norm.z /= normalLength;
                    norm.x /= normalLength;
                    norm.y /= normalLength;

                    double angleCos = Math.abs(norm.z);
                    
                    // compute bounds for Triangle.
                    int minX = (int) Math.max(0,Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));

                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    for(int y = minY; y <= maxY; y++){
                        for(int x = minX; x <= maxX; x++){
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, getShade(t.color,angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }
                g2.drawImage(img, 0, 0, null);
            }
        };

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        TetrahedronButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentShape = Shape.Tetrahedron;
                renderPanel.repaint();
            }
        });

        cubeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentShape = Shape.Cube;
                renderPanel.repaint();
            }
        });

        sphereButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentShape = Shape.Sphere;
                renderPanel.repaint();
            }
        });

        wireframeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wireframe = !wireframe;
                if(wireframe)
                    wireframeButton.setText("Solid");
                else
                    wireframeButton.setText("Wireframe");
                renderPanel.repaint();
            }
        });

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400,426);
        frame.setTitle("3D Rendering");
        frame.setVisible(true);

    }

    public static Color getShade(Color color, double shade){
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }

    public static ArrayList inflate(ArrayList<Triangle> tris) {
        ArrayList<Triangle> result = new ArrayList<Triangle>();

        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x)/2, (t.v1.y + t.v2.y)/2, (t.v1.z + t.v2.z)/2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x)/2, (t.v2.y + t.v3.y)/2, (t.v2.z + t.v3.z)/2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x)/2, (t.v1.y + t.v3.y)/2, (t.v1.z + t.v3.z)/2);

            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }
        for (Triangle t : result) {
            for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }

    private static ArrayList createTetrahedronList(){
        ArrayList<Triangle> tris = new ArrayList<Triangle>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));
        return tris;
    }

    private static ArrayList createCubeList(){
        ArrayList<Triangle> tris = new ArrayList<Triangle>();
        // todo: create a square
        return tris;
    }

    private static int sphereDetail = 4;

    private static ArrayList createSphereList(){
        ArrayList<Triangle> tris = new ArrayList<Triangle>();
        tris = createTetrahedronList();
        for(int i = 0; i < sphereDetail; i++)
            tris = inflate(tris);
        return tris;
    }

    private static ArrayList createEmptyList(){
        ArrayList<Triangle> tris = new ArrayList<Triangle>();
        return tris;
    }

}
