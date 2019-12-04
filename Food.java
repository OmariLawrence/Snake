import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.JFrame;
import java.applet.Applet;
import java.applet.AudioClip;

public class Food extends Sprite {

    AudioClip hitWallSound = null;
    Random random;

    public Food (JFrame f, int x, int y, int dx, int dy, int xSize, int ySize, String filename) {
        super(f, x, y, dx, dy, xSize, ySize, filename);

        random = new Random();
        setPosition();
    }

    @Override
    public void draw (Graphics g) {
        g.setColor(Color.RED);
        
        g.fillRect( x, y, xSize, ySize);
    }

    public void setPosition(){
        x = random.nextInt(super.WWidth);
        y = random.nextInt(super.WHeight);

        x = x - (x%20);
        y = y - (y%20);
    }
    
    public void update(){
    
    }

}