package src;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Main {

    public enum Shape {Tetrahedron, Cube}

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
        JPanel buttonPanel = new JPanel(new GridLayout(0,3,2,0));
        buttonPanel.setBackground(Color.black);

        // The button to change the shape to Tetrahedron.
        JButton TetrahedronButton = new JButton("Tetrahedron");
        buttonPanel.add(TetrahedronButton);

        // The button to change the shape to Cube.
        JButton CubeButton = new JButton("Cube");
        buttonPanel.add(CubeButton);

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
                else
                    tris = createTetrahedronList();

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

                for(Triangle t :    tris){
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);
                    
                    //have to manually translate since the image is not a Graphics2D object.
                    v1.x += component.getWidth() / 2;
                    v1.y += component.getHeight() / 2;
                    v2.x += component.getWidth() / 2;
                    v2.y += component.getHeight() / 2;
                    v3.x += component.getWidth() / 2;
                    v3.y += component.getHeight() / 2;

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
                                    img.setRGB(x, y, t.color.getRGB());
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

}
