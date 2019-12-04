import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import java.util.ArrayList;
import javax.swing.JFrame;
import java.applet.Applet;
import java.applet.AudioClip;

public class Snake extends Sprite {

    AudioClip hitWallSound = null;
    Random random;
    private ArrayList<Rectangle2D.Double> snakeBody;;
    private int snakeSize;
    private boolean hit;
    private int immunity;

    public Snake (JFrame f, int x, int y, int dx, int dy, int xSize, int ySize, String filename) {
        super(f, x, y, dx, dy, xSize, ySize, filename);

        snakeBody = new ArrayList<>();
        snakeSize = 3;
        immunity = 5;
        hit = false;

        random = new Random();
        initSnake();
        loadClips();
    }

    public void initSnake(){
        snakeBody.add(new Rectangle2D.Double( x, y, xSize, ySize));
        snakeBody.add(new Rectangle2D.Double( x+20, y, xSize, ySize));
        snakeBody.add(new Rectangle2D.Double( x+40, y, xSize, ySize));
    }

    public void makeLonger(){
        snakeBody.add(new Rectangle2D.Double(snakeBody.get(snakeSize-1).x, snakeBody.get(snakeSize-1).y, xSize, ySize));
        snakeSize++;
    }
    
    public boolean collided(){
        Rectangle2D head = snakeBody.get(0);
        for(int i = 1; i < snakeSize; i++){
            if(head.intersects(snakeBody.get(i)) && immunity < 1){
                hit = true;
                System.out.println("Hit number: " + i);
            }
        }

        return hit;
    }

    @Override
    public void draw (Graphics g) {
        g.setColor(Color.GREEN);
        
        for(int i = 0; i < snakeSize; i++){
            g.fillRect((int)snakeBody.get(i).x, (int)snakeBody.get(i).y, (int)snakeBody.get(i).width, (int)snakeBody.get(i).height);
        }
    }

    public int getSize(){
        return snakeSize;
    }

    public Rectangle2D.Double getRect(int i){
        if(i >= snakeSize)
            return null;

        return snakeBody.get(i);
    }

    public void update() {
        immunity--;
        
        if(snakeBody.get(0) == null)
            return;
        
        Double prevx = snakeBody.get(0).x;
        Double prevy = snakeBody.get(0).y;
        Double tempx, tempy;
        
        snakeBody.get(0).x = snakeBody.get(0).x + dx;
        snakeBody.get(0).y = snakeBody.get(0).y + dy;

        if(snakeBody.get(0).y <= 0) {                   // hits left wall
            snakeBody.get(0).y = 0;
            playClip(1);
        }
        if(snakeBody.get(0).y >= super.WWidth){      //hits right wall
            x = super.WWidth;
            playClip(1);
        }
        if(snakeBody.get(0).y <= 0) {                   // hits top wall
            y = 0;
            playClip(1);
        }
        if(snakeBody.get(0).y >= super.WHeight){     //hits bottom wall
            snakeBody.get(0).y = super.WHeight;
            playClip(1);
        }

        for(int i = 1; i < snakeSize; i++){
            tempx = snakeBody.get(i).x;
            tempy = snakeBody.get(i).y;

            snakeBody.get(i).x = prevx;
            snakeBody.get(i).y = prevy;

            prevx = tempx;
            prevy = tempy;
        }

    }

    public void changeDirection(int dx, int dy) {

        if (!window.isVisible ()) return;

        this.dx = dx;
        this.dy = dy;
        
    }

    public void loadClips() {

        try {
            hitWallSound = Applet.newAudioClip (
                        getClass().getResource("sounds/hitWall.au"));
        }
        catch (Exception e) {
            System.out.println ("Error loading sound file: " + e);
        }

    }

    public void playClip(int index) {

        if (index == 1 && hitWallSound != null)
            hitWallSound.play();
    }

}