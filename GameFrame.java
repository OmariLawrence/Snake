import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.geom.Rectangle2D;

public class GameFrame extends JFrame implements Runnable, KeyListener {
  	private static final int NUM_BUFFERS = 2;	// used for page flipping

	private int pWidth, pHeight;     		// dimensions of screen

	private Thread gameThread = null;            	// the thread that controls the game
	private volatile boolean running = false;    	// used to stop the animation thread

	private Snake snake;				// snake sprite
	private Food food;					// food sprite
	private Image bgImage;				// background image
	AudioClip playSound = null;			// theme sound

  	// used at game termination
	private boolean finishedOff = false;
	private boolean isPaused = false;
	private boolean isStopped = false;
  
	// used for full-screen exclusive mode  
	private GraphicsDevice device;
	private Graphics gScr;
	private BufferStrategy bufferStrategy;
	
	private int snakeSize = 20;
	private int score = 0;

	public GameFrame () {
		super("snake and Ball Game: Full Screen Exclusive Mode");

		initScreen();

		// create game sprites

		snake = new Snake(this, 0, 120, snakeSize, 0, snakeSize, snakeSize, "images/ball.gif");
		food = new Food(this, 200, 200, 0, 0, snakeSize, snakeSize, "images/ball.gif"); 

		addKeyListener(this);			// respond to key events

		loadImages();
		loadClips();
		startGame();
	}

	private void initScreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		device = ge.getDefaultScreenDevice();

		//setUndecorated(true);	// no menu bar, borders, etc.
		setIgnoreRepaint(true);	// turn off all paint events since doing active rendering
		setResizable(false);	// screen cannot be resized
		setTitle("Snake");
		setSize(400, 400);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// we can now adjust the display modes, if we wish

		showCurrentMode();

		pWidth = getBounds().width;
		pHeight = getBounds().height;

		try {
			createBufferStrategy(NUM_BUFFERS);
		}
		catch (Exception e) {
			System.out.println("Error while creating buffer strategy " + e); 
			System.exit(0);
		}

		bufferStrategy = getBufferStrategy();
	}

	// this method creates and starts the game thread

	private void startGame() { 
		if (gameThread == null || !running) {
			gameThread = new Thread(this);
			gameThread.start();
			playSound.loop();
		}
	}
	
	// implementation of KeyListener interface

	public void keyPressed (KeyEvent e) {

		int keyCode = e.getKeyCode();
         
		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) ||
             	   (keyCode == KeyEvent.VK_END)) {
           		running = false;		// user can quit anytime by pressing
			return;				//  one of these keys (ESC, Q, END)
         	}	

		if (snake == null)		
			// don't do anything if either condition is true
			return;

		if (keyCode == KeyEvent.VK_LEFT) {
			snake.changeDirection(-snakeSize,0);
		}
		if (keyCode == KeyEvent.VK_RIGHT) {
			snake.changeDirection(snakeSize,0);
		}
		if(keyCode == KeyEvent.VK_UP){
			snake.changeDirection(0,-snakeSize);
		}
		if(keyCode == KeyEvent.VK_DOWN){
			snake.changeDirection(0,snakeSize);
		}

	}

	public void keyReleased (KeyEvent e) {

	}

	public void keyTyped (KeyEvent e) {

	}


	// implmentation of MousePressedListener interface

	// implmentation of MouseMotionListener interface

	// The run() method implements the game loop.

	public void run() {

		running = true;
		try {
			while (running) {
	  			gameUpdate();     
	      			screenUpdate();
				Thread.sleep(300);
			}
		}
		catch(InterruptedException e) {};

		finishOff();
	}


	// This method updates the game objects (animation and ball)

	private void gameUpdate() { 

		if (!isPaused) {
			if (!isStopped){
				this.getFood();
				snake.update();
				if(snake.collided())
					isStopped = true;
			}
		}
  	}
	
	private void getFood(){
		Rectangle2D fRect = food.getRect();
		Rectangle2D sRect = snake.getRect(0);
		if(fRect.intersects(sRect) || sRect.intersects(fRect)){
			PositionFood();
			snake.makeLonger();
			score++;
		}
	}

	private void PositionFood(){
		food.setPosition();
		Rectangle2D fRect = food.getRect();
		Rectangle2D sRect = snake.getRect(0);
		for(int i = 0; i < snake.getSize(); i++){
			sRect = snake.getRect(i);
			if(fRect.intersects(sRect) || sRect.intersects(fRect)){
				food.setPosition();
			}
		}
	}

	// This method updates the screen using double buffering / page flipping

	private void screenUpdate() { 

		try {
			gScr = bufferStrategy.getDrawGraphics();
			gameRender(gScr);
			gScr.dispose();
			if (!bufferStrategy.contentsLost())
				bufferStrategy.show();
			else
				System.out.println("Contents of buffer lost.");
      
			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)

			Toolkit.getDefaultToolkit().sync();
		}
		catch (Exception e) { 
			e.printStackTrace();  
			running = false; 
		} 
	}

	/* This method renders all the game entities to the screen: the
	   background image, the buttons, ball, snake, and the animation.
	*/

	private void gameRender(Graphics gScr){
 
		gScr.drawImage (bgImage, 0, 0, pWidth, pHeight, null);
							// draw the background image

		gScr.setColor(Color.black);

		food.draw((Graphics2D)gScr);		// draw the food
		snake.draw((Graphics2D)gScr);		// draw the snake
		printScore(gScr);
					
		if (isStopped)				// display game over message
			gameOverMessage(gScr);
	}

	private void printScore(Graphics g) {
		
		Font font = new Font("SansSerif", Font.BOLD, 24);
		FontMetrics metrics = this.getFontMetrics(font);

		String msg = "Score: " + score;

		int x = (400 - metrics.stringWidth(msg)) / 2; 
		int y = 50;

		g.setColor(Color.BLUE);
		g.setFont(font);
		g.drawString(msg, x, y);

	}

		// displays a message to the screen when the user stops the game

	private void gameOverMessage(Graphics g) {
		
		Font font = new Font("SansSerif", Font.BOLD, 24);
		FontMetrics metrics = this.getFontMetrics(font);

		String msg = "Game Over. Thanks for playing!";

		int x = (400 - metrics.stringWidth(msg)) / 2; 
		int y = (400 - metrics.getHeight()) / 2;

		g.setColor(Color.BLUE);
		g.setFont(font);
		g.drawString(msg, x, y);

	}
	
	/* This method performs some tasks before closing the game.
	   The call to System.exit() should not be necessary; however,
	   it prevents hanging when the game terminates.
	*/

	private void finishOff() { 
    		if (!finishedOff) {
				finishedOff = true;
				restoreScreen();
				System.exit(0);
		}
	}

	/* This method switches off full screen mode. The display
	   mode is also reset if it has been changed.
	*/

	private void restoreScreen() { 
		Window w = device.getFullScreenWindow();
		
		if (w != null)
			w.dispose();
		
		device.setFullScreenWindow(null);
	}

	// This method provides details about the current display mode.

	private void showCurrentMode() {
		DisplayMode dm = device.getDisplayMode();
		System.out.println("Current Display Mode: (" + 
                           dm.getWidth() + "," + dm.getHeight() + "," +
                           dm.getBitDepth() + "," + dm.getRefreshRate() + ")  " );
  	}
	
	public void loadImages() {

		bgImage = loadImage("images/newbgimage.jpg");
	}

	public Image loadImage (String fileName) {
		return new ImageIcon(fileName).getImage();
	}

	public void loadClips() {

		try {
			playSound = Applet.newAudioClip (
					getClass().getResource("sounds/background.wav"));

		}
		catch (Exception e) {
			System.out.println ("Error loading sound file: " + e);
		}

	}

	public void playClip (int index) {

		if (index == 1 && playSound != null)
			playSound.play();

	}

}

