package fr.jamailun.pong.server;

import java.awt.Rectangle;

public class GameDataManager {
	
	Rectangle map, rec1, rec2, ball;
	int dx, dy;
	int scoreL, scoreR;
	
	private static final int MAP_HEIGHT = 500;
	private static final int MAP_WIDTH = 800;
	
	private static final int BALL = 15;
	
	private static final int DISTANCE_FROM_SIZES = 40;
	private static final int TILE_WIDTH = 20;
	private static final int TILE_HEIGHT = 80;
	
	private static final int CORRECTION = 28; //décalage à cause de la bordure de la JFrame
	
	public GameDataManager() {
		init();
	}
	
	public void reboot() {
		init();
	}
	
	private void init() {
		map = new Rectangle(0, 0, MAP_WIDTH, MAP_HEIGHT);
		rec1 = new Rectangle(DISTANCE_FROM_SIZES, (MAP_HEIGHT / 3), TILE_WIDTH, TILE_HEIGHT);
		rec2 = new Rectangle(MAP_WIDTH - DISTANCE_FROM_SIZES - TILE_WIDTH, (MAP_HEIGHT / 3), TILE_WIDTH, TILE_HEIGHT);
		ball = new Rectangle((MAP_WIDTH - BALL) / 2, (MAP_HEIGHT + BALL) / 2, BALL, BALL);
		scoreL = scoreR = 0;
		resetSpeeds();
	}
	
	public void mooveTile(int player, int how) {
		if((player != 1) && (player != 2))
			return;
		Rectangle tile = rec1;
		if(player == 2)
			tile = rec2;
		
		int x = (int) tile.getX();
		int y = (int) tile.getY();
		
		y += how;
		if(y < 0)
			tile.setLocation(x, 0);
		else if(y > MAP_HEIGHT - TILE_HEIGHT - CORRECTION)
			tile.setLocation(x, MAP_HEIGHT - TILE_HEIGHT - CORRECTION);
		else
			tile.setLocation(x, y);
	}
	
	public void refreshBall() {
		if(reseting)
			return;
		final Rectangle r1 = rec2, r2 = rec1;
		int x = (int) ball.getX();
		int y = (int) ball.getY();
		
		//MOUVEMENT
		x += (dx / 2);
		y += (dy / 1.5);
		
		//MAP
		if(x <= 0) {
			scoreR++;
			resetAfterScore();
			return;
		} else if(x + BALL >= MAP_WIDTH) {
			scoreL++;
			resetAfterScore();
			return;
		}
		
		if(y <= 0) {
			y = 0;
			dy = -dy;
		} else if(y + BALL >= MAP_HEIGHT - CORRECTION) {
			y = MAP_HEIGHT - BALL - CORRECTION;
			dy = -dy;
		}
		
		//TILES
		if(r2.intersects(ball)) {
			x = r2.x + r2.width + 1;
			dx = -(dx - randInt( 1 , Math.abs(dy / 3)));
			dy = getAngleY(dy, ball.y, r2);
		} else if(r1.intersects(ball)) {
			x = r1.x - BALL - 1;
			dy = getAngleY(dy, ball.y, r1);
			dx = -(dx + randInt( 1 , Math.abs(dy / 3)));
		}
		
		//POS finale
		ball.setLocation(x, y);
	}
	
	private int getAngleY(int dy, int ballY, final Rectangle tile) {
		final int centralBallY = ballY + (BALL / 2);
		final int centralTileY = tile.y + (tile.height / 2);
		
		int sign = 1;
		if(dy < 0)
			sign = -1;
		
		float diffY = Math.abs(centralTileY - centralBallY);
		
		dy = (int) ( sign * (( (float) (diffY / (tile.height / 2))) * 10 ) );
		
		return dy;
	}
	
	public String getMapData() {
		//avec: map_height, map_width, tile_height, tile_width, ball_size
		return MAP_HEIGHT + "," + MAP_WIDTH + "," + TILE_HEIGHT + "," + TILE_WIDTH + "," + BALL;
	}

	public String getObjectsData() {
		//avec: tile1_x, tile1_y, tile2_x, tile2_y, ball_x, ball_y
		try {
			return rec1.x + "," + rec1.y + "," + rec2.x + "," + rec2.y + "," + ball.x + "," + ball.y + "," + scoreL + "," + scoreR;
		} catch(NullPointerException e) {
			e.printStackTrace();
			return "ERROR";
		}
	}
	
	private int randInt(int min, int max) {
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	
	private boolean reseting = false;
	private void resetAfterScore() {
		reseting = true;
		rec1.setLocation(DISTANCE_FROM_SIZES, (MAP_HEIGHT / 3));
		rec2.setLocation(MAP_WIDTH - DISTANCE_FROM_SIZES - TILE_WIDTH, (MAP_HEIGHT / 3));
		ball.setLocation((MAP_WIDTH - BALL) / 2, (MAP_HEIGHT + BALL) / 2);
		resetSpeeds();
		Thread pause = new Thread(new Pause(this, 1000));
		pause.start();
	}
	
	private void resetSpeeds() {
		dx = dy = 0;
		while(dx >= -5 && dx <= 5)
			dx = randInt(-10, 10);
		while(dy >= -2 && dy <= 2)
			dy = randInt(-6, 6);
	}
	
	public Rectangle getBall() {
		return ball;
	}
	
	public Rectangle getTile1() {
		return rec1;
	}
	
	public Rectangle getTile2() {
		return rec2;
	}
	
	public Rectangle getMap() {
		return map;
	}
	
	private class Pause implements Runnable {
		private GameDataManager gdm;
		private int time;
		public Pause(GameDataManager gdm, int time) {
			this.gdm = gdm;
			this.time = time;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(time);
				gdm.endReseting();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void endReseting() {
		reseting = false;
	}

}
