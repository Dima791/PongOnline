package fr.jamailun.pong.server;

import fr.jamailun.pong.server.PongServerMain.State;

public class GameWatcher implements Runnable {
	
	private GameDataManager mgr;
	
	public GameWatcher(GameDataManager mgr) {
		this.mgr = mgr;
	}

	@Override
	public void run() {
		try {
			while(PongServerMain.state != State.STOP) {
				
				Thread.sleep(20);
				
				mgr.refreshBall();
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
