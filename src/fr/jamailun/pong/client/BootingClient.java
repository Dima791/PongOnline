package fr.jamailun.pong.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import fr.jamailun.pong.client.PongClientMain.State;
import fr.jamailun.pong.values.PongLibrairies;
import fr.jamailun.pong.values.Proto;

public class BootingClient implements Runnable {
	private DatagramSocket client;
	public boolean aborded;
	private PongPanel panel;
	
	public BootingClient(PongPanel panel) {
		aborded = false;
		this.panel = panel;
		try {
			client = new DatagramSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			
			while(PongClientMain.state == State.BOOT) {
				panel.displayMsg = "Sending handshake...";
				
				Thread.sleep(750);
				
				//Envoie de l'handshake au serveur
				String handshakeData = Proto.HANDSHAKE.n + "@" + PongClientMain.pc;
				byte[] buffer = handshakeData.getBytes();
				DatagramPacket handshakePacket = new DatagramPacket(
						buffer, 
						buffer.length, 
						InetAddress.getByName(PongLibrairies.$publicServerIp),
						PongLibrairies.$publicServerPort
				);
				client.send(handshakePacket);
				
				byte[] buffer2 = new byte[PongLibrairies.$packetSize];
				DatagramPacket aswPacket = new DatagramPacket(
						buffer2,
						buffer2.length,
						InetAddress.getByName(PongLibrairies.$publicServerIp),
						PongLibrairies.$publicServerPort
				);
				System.out.println("[CLIENT][BOOT] Sending handshake.");
				
				Thread pause = new Thread(new Pause(this, 5000));
				pause.start();
				
				try {
					client.receive(aswPacket);
				} catch(SocketException e) {
					System.err.println("[CLIENT][BOOT] Timeout error !");
					return;
				}
				if(aborded) {
					client.close();
					return;
				}
				
				//Attente packet...
				
				String answer = PongLibrairies.removeSpaces(aswPacket.getData());
				System.out.println("[CLIENT][BOOT] Answer from handshake : " + answer);

				if(answer.startsWith("true")) {

					panel.displayMsg = "Handshake is valid !";
					String[] all = answer.split("@", 2);
					
					PongClientMain.state = State.WAITING_GAME;
					PongClientMain.connected(Integer.parseInt(all[1]));
					
					client.close();
				} else {
					panel.displayMsg = "Error:" + answer;
					System.out.println("[CLIENT][ERROR] Answer from handshake: "+answer);
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				Thread.sleep(1500);
				
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void restartHandShake() {
		client.close();
		panel.displayMsg = "Timeout !";
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		PongClientMain.resendHandshake();
	}
	
	private class Pause implements Runnable {
		private BootingClient bc;
		private int time;
		public Pause(BootingClient bc, int time) {
			this.bc = bc;
			this.time = time;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(time);
				if(PongClientMain.state != State.PLAYING) {
					bc.aborded = true;
					bc.restartHandShake();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

