package src;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public enum Shape {Tetrahedron, placeholder01, placeholder02}

    Shape currentShape = Shape.Tetrahedron;

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

        JPanel buttonPanel = new JPanel(new GridLayout(0,3,2,0));
        buttonPanel.setBackground(Color.black);

        JButton TetrahedronButton = new JButton("Tetrahedron");
        buttonPanel.add(TetrahedronButton);

        JButton placeholder01Button = new JButton("Placeholder01");
        buttonPanel.add(placeholder01Button);

        JButton placeholder02Button = new JButton("Placeholder02");
        buttonPanel.add(placeholder02Button);

        pane.add(buttonPanel,BorderLayout.NORTH);

        // panel to display rendering result.
        JPanel renderPanel = new JPanel(){
            public void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.black);
                g2.fillRect(0, 0, getWidth(), getHeight());

                //render here
                
                g2.translate(getWidth()/2, getHeight()/2);
                g2.setColor(Color.WHITE);

                ArrayList<Triangle> tris = createTetrahedronList();

                for(Triangle t : tris){
                    Path2D path = new Path2D.Double();
                    path.moveTo(t.v1.x, t.v1.y);
                    path.lineTo(t.v2.x, t.v2.y);
                    path.lineTo(t.v3.x, t.v3.y);
                    path.closePath();
                    g2.draw(path);

                }

            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400,426);
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

}
