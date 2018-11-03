package fr.jamailun.pong.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.jamailun.pong.client.PongClientMain.State;

public class PongPanel extends JPanel implements Runnable, KeyListener {
	private static final long serialVersionUID = 1L;
	
	private JFrame window;
	
	public String displayMsg = "";
	
	private Graphics2D g;
	private BufferedImage image;
	private Thread thread;
	
	private Rectangle rec1, rec2, ball, mapSize;
	private int ballSize, scoreL, scoreR;
	private final Font font;
	
	private int currentTileMovement = 0; //si 1+, la barre monte. si 1-, elle descend. on envoie l'info régulièrement au serv.
	
	public PongPanel(JFrame window) {
		this.window = window;
		running = false;
		
		g = (Graphics2D) window.getGraphics();

		window.setSize(600,  200);
		window.setPreferredSize(new Dimension(600, 200));
		window.pack();
		
		font = new Font("Lucida Console", Font.PLAIN, 20);
	}
	
	
	
	private boolean running;
	private long systemClock = 1000 / 30;
	@Override
	public void run() {
		if(running)
			return;
		running = true;
		
		long start;
		long elapsed;
		long wait;
		
		//boucle
		while(running) {
			
			start = System.nanoTime();
			//L'update avec le serveur se fait via le Listenner
			draw();
			drawToScreen();
			
			elapsed = System.nanoTime() - start;
			wait = systemClock - elapsed / 1000000;
			
			if(wait > 0) {
				try{
					Thread.sleep(wait);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	public void infoServer$gameStart(String packetData) {
		//avec: map_height, map_width, tile_height, tile_width, ball_size
		String[] data = packetData.split(",", 5);
		try {
			int mapHeight = Integer.parseInt(data[0]);
			int mapWidth = Integer.parseInt(data[1]);
			int tileHeight = Integer.parseInt(data[2]);
			int tileWidth = Integer.parseInt(data[3]);
			ballSize = Integer.parseInt(data[4]);
			
			mapSize = new Rectangle(0, 0, mapWidth, mapHeight);
			
			rec1 = new Rectangle(0, 0, tileWidth, tileHeight);
			rec2 = new Rectangle(50, 0, tileWidth, tileHeight);
			ball = new Rectangle(100, 0, ballSize, ballSize);
			scoreL = 0;
			scoreR= 0;

			window.setSize(mapWidth, mapHeight);
			window.setPreferredSize(new Dimension(mapWidth, mapHeight));
			window.pack();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void infoServer$updateObjects(String packetData) {
		if(packetData.equals("ERROR"))
			return;
		//avec: tile1_x, tile1_y, tile2_x, tile2_y, ball_x, ball_y, scoreL, scoreR
		String[] data = packetData.split(",", 8);
		try {
			int tile1x = Integer.parseInt(data[0]);
			int tile1y = Integer.parseInt(data[1]);
			int tile2x = Integer.parseInt(data[2]);
			int tile2y = Integer.parseInt(data[3]);
			int ballx = Integer.parseInt(data[4]);
			int bally = Integer.parseInt(data[5]);
			scoreL = Integer.parseInt(data[6]);
			scoreR = Integer.parseInt(data[7]);
			
			rec1.setLocation(tile1x, tile1y);
			rec2.setLocation(tile2x, tile2y);
			ball.setLocation(ballx, bally);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void updateInfoAboutKeyboard() {
		if(upPressed)
			currentTileMovement--;
		if(downPressed)
			currentTileMovement++;
	}
	
	public int getCurrentTileMovement() {
		return currentTileMovement;
	}
	
	public void resetCurrentTileMovement() {
		currentTileMovement = 0;
	}
	
	public void draw() {
		image = new BufferedImage(window.getWidth(), window.getHeight(), BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, window.getWidth(), window.getHeight());
		
		if(mapSize != null) {
			g.setColor(Color.WHITE);
			g.fillRect(rec1.x, rec1.y, rec1.width, rec1.height);
			g.fillRect(rec2.x, rec2.y, rec2.width, rec2.height);
			g.fillRect(ball.x, ball.y, ballSize, ballSize);
			
			g.setFont(font);
			g.drawString(""+scoreL, 5, 22);
			g.drawString(""+scoreR, mapSize.width - 25, 22);
			
		} else {
			if(PongClientMain.state == State.WAITING_GAME) {
				g.setColor(Color.GREEN);
				g.drawString("Connecté. En attente d'un autre joueur.", 25, 30);
			} else {
				g.setColor(Color.RED);
				g.drawString("Non connecté", 25, 30);
				g.setColor(Color.BLUE);
				if(displayMsg.equals("Timeout !"))
					g.setColor(Color.RED);
				g.drawString(displayMsg, 25, 50);
			}
		}
	}
	
	private void drawToScreen() {
		Graphics g2 = getGraphics();
		if(g2 != null) {
			g2.drawImage(image, 0, 0, window.getWidth(), window.getHeight(), null);
			g2.dispose();
		}
	}

	@Override
	public void keyTyped(KeyEvent k) {}

	private boolean upPressed = false;
	private boolean downPressed = false;
	
	@Override
	public void keyPressed(KeyEvent k) {
		if(k.getKeyCode() == KeyEvent.VK_UP) {
			upPressed = true;
		} else if(k.getKeyCode() == KeyEvent.VK_DOWN) {
			downPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent k) {
		if(k.getKeyCode() == KeyEvent.VK_UP) {
			upPressed = false;
		} else if(k.getKeyCode() == KeyEvent.VK_DOWN) {
			downPressed = false;
		}
	}
	
	public void addNotify() {
		super.addNotify();
		if(thread == null) {
			thread = new Thread(this);
			this.setFocusable(true);
			this.addKeyListener(this);
			thread.start();
		}
	}

	public void deconnect() {
		rec1 = rec2 = ball = mapSize = null;
		ballSize = 0;
		window.setSize(600,  200);
		window.setPreferredSize(new Dimension(600, 200));
		window.pack();
	}
	
}
