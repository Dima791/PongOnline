package fr.jamailun.pong.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import fr.jamailun.pong.client.PongClientMain.State;
import fr.jamailun.pong.values.PongLibrairies;
import fr.jamailun.pong.values.Proto;

public class ClientListener implements Runnable {
	
	private PongPanel panel;
	private DatagramSocket client;
	private int port, serverPort;
	
	public ClientListener(PongPanel panel, int port, int serverPort) { //port du serv qui ENVOIE au client du coup xd
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
			
			System.out.println("[CLIENT][INFO] Starting listening on port " + port + ". Serveur is sending from port " + serverPort + ".");
			
			while(PongClientMain.state != State.STOPING) {
				
				byte[] buffer = new byte[PongLibrairies.$packetSize];
				DatagramPacket packet = new DatagramPacket(
						buffer,
						buffer.length,
						InetAddress.getByName(PongLibrairies.$publicServerIp),
						serverPort
				);
				client.receive(packet);
				
				System.out.println("[DEBUG] j'ai recu quelque chooooose");
				
				String get = PongLibrairies.removeSpaces(packet.getData());
				String[] data = get.split("@", 2);
				if(data.length < 2) {
					System.err.println("[CLIENT][ERROR] Incorrect data : " + get);
					continue;
				}
				Proto protocol = Proto.getWithNumber(Integer.parseInt(data[0]));
				if(protocol == Proto.GAME_START) {
					panel.infoServer$gameStart(data[1]);
				} else if(protocol == Proto.OBJECTS) {
					panel.infoServer$updateObjects(data[1]);
				} else if(protocol == Proto.STOP_CONNEXION) {
					PongClientMain.deconnect();
				}
				
				
			}
			
			client.close();
			PongClientMain.closedL = true;
			return;
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
