package fr.jamailun.pong.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import fr.jamailun.pong.client.PongClientMain.State;
import fr.jamailun.pong.values.PongLibrairies;
import fr.jamailun.pong.values.Proto;

public class ClientSender  implements Runnable {
	
	private PongPanel panel;
	private DatagramSocket client;
	private int port, serverPort;
	
	public ClientSender(PongPanel panel, int port, int serverPort) {
		this.panel = panel;
		this.port = port;
		this.serverPort = serverPort;
		try {
			client = new DatagramSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			
			System.out.println("[CLIENT][INFO] Starting sending on port " + port + ". Serveur is listening on port " + serverPort + ".");
			
			while(PongClientMain.state == State.WAITING_GAME) {
			//System.out.println(">> Sending READY? sur ["+serverPort+"], par ["+client.getLocalPort()+"]");
				Thread.sleep(400);
				sendPacket(Proto.READY.n + "@0");
				
				DatagramPacket packet = newPacketToListen();
				sending = true;
				(new Thread(new TimeoutGetter(5000, this))).start();
				try {
					client.receive(packet);
				} catch(SocketException e) {
					sending = false;
					return;
				}
				sending = false;
				
				//System.out.println("<< READY? ok .");
				
				String get = PongLibrairies.removeSpaces(packet.getData());
				String[] answer = get.split("@", 2);
				Proto protocol = Proto.getWithNumber(answer[0]);
				
				if(protocol == Proto.GAME_START) {
					panel.infoServer$gameStart(answer[1]);
					PongClientMain.state = State.PLAYING;
					System.out.println("[CLIENT][INFO] The game started !");
				}
				
			}
			//System.out.println("dzadzadzadzadzad.");
			
			Thread.sleep(200);
			
			while(PongClientMain.state == State.PLAYING) {
				for(int i = 1; i <= 25; i++) {
					Thread.sleep(2);
					panel.updateInfoAboutKeyboard();
				}
				
				int movement = panel.getCurrentTileMovement();
				panel.resetCurrentTileMovement();
				
				//System.out.println(">> Sending MOOVETILE .");
				
				String data = Proto.MOOVE_TILE.n + "@" + movement;
				sendPacket(data);
				
				DatagramPacket packet = newPacketToListen();
				sending = true;
				(new Thread(new TimeoutGetter(500, this))).start();
				try {
					client.receive(packet);
				} catch(SocketException e) {
					sending = false;
					return;
				}
				sending = false;
				

				//System.out.println("<< MOOVETILE? ok .");
				
				String get = PongLibrairies.removeSpaces(packet.getData());
				String[] answer = get.split("@", 2);
				Proto protocol = Proto.getWithNumber(answer[0]);
				
				if(protocol == Proto.OBJECTS) {
					panel.infoServer$updateObjects(answer[1]);
				}
				
				else if(protocol == Proto.STOP_CONNEXION) {
					PongClientMain.deconnect();
					Thread.sleep(100);
					return;
				}
			}
			
			System.out.println("Stopping ClientSender");
			
			String data = Proto.STOP_CONNEXION.n + "@0";
			byte[] buffer = data.getBytes();
			DatagramPacket packet = new DatagramPacket(
					buffer,
					buffer.length,
					InetAddress.getByName(PongLibrairies.$publicServerIp),
					serverPort
			);
			client.send(packet);
			client.close();
			PongClientMain.closedS = true;
			return;
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void sendPacket(String data) {
		try {
			byte[] buffer = data.getBytes();
			DatagramPacket packet = new DatagramPacket(
					buffer,
					buffer.length,
					InetAddress.getByName(PongLibrairies.$publicServerIp),
					serverPort
			);
			client.send(packet);
		} catch (IOException e) {}
	}
	
	private synchronized DatagramPacket newPacketToListen() {
		byte[] buffer = new byte[PongLibrairies.$packetSize];
		try {
			return new DatagramPacket(
					buffer,
					buffer.length,
					InetAddress.getByName(PongLibrairies.$publicServerIp),
					serverPort
			);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private synchronized void newTimeout() {
		client.close();
		sending = false;
		PongClientMain.timeoutOnSender();
	}
	
	private boolean sending;
	private class TimeoutGetter implements Runnable {
		private int time;
		private ClientSender cs;
		public TimeoutGetter(int time, ClientSender cs) {
			this.time = time;
			this.cs = cs;
		}
		public void run() {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {e.printStackTrace();}
			if(sending)
				cs.newTimeout();
		}
	}
	
}
