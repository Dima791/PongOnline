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
			check = 50;
			while(PongServerMain.state != State.STOP) {
				
				Thread.sleep(20);
				
				mgr.refreshBall();
				
				check--;
				final long last1 = PongServerMain.client1.getLastKeepAlive();
				final long last2 = PongServerMain.client2.getLastKeepAlive();
				final long now = System.currentTimeMillis();
				if(check <= 0) {
					//après 10 sec : timeout !
					if(now - last1 >= 10000) {
						System.out.println("[SERVER][WARN] Client 1 has timed out !");
						PongServerMain.disconnect(PongServerMain.client1);
					}
					if(now - last2 >= 10000) {
						System.out.println("[SERVER][WARN] Client 2 has timed out !");
						PongServerMain.disconnect(PongServerMain.client2);
					}
					check = 0;
				}
				
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
