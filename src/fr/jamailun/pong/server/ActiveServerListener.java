package fr.jamailun.pong.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import fr.jamailun.pong.server.PongServerMain.State;
import fr.jamailun.pong.values.PongLibrairies;
import fr.jamailun.pong.values.Proto;

public class ActiveServerListener implements Runnable {
	
	private ClientInfo client;
	private GameDataManager mgr;
	
	private DatagramSocket server;
	
	public ActiveServerListener(int port, ClientInfo client, GameDataManager mgr) {
		this.client = client;
		this.mgr = mgr;
		try {
			server = new DatagramSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Bonjour, j'attends sur ["+port+"], et mon client m'appelle par ["+client.getPort()+"]");
	}
	
	@Override
	public void run() {
		try {
			while(PongServerMain.state != State.STOP) {
				
				Thread.sleep(50);
				
				byte[] buffer = new byte[PongLibrairies.$packetSize];
				DatagramPacket packet = new DatagramPacket(
						buffer,
						buffer.length,
						client.getAdress(),
						client.port
				);
				server.receive(packet);
				
				
				String get = PongLibrairies.removeSpaces(packet.getData());
				String[] data = get.split("@", 2);
				if(data.length < 2) {
					System.err.println("[SERVER][ERROR] Incorrect packet : " + get);
					continue;
				}
				
				//System.out.println("[>] Got packet from " + packet.getAddress().getHostAddress() +"|"+packet.getPort() +". Packet: "+get);
				
				
				Proto protocol = Proto.getWithNumber(Integer.parseInt(data[0]));
				
				if(protocol == Proto.MOOVE_TILE) {
					if(client.equals(PongServerMain.client1))
						mgr.mooveTile(1, Integer.parseInt(data[1]));
					else
						mgr.mooveTile(2, Integer.parseInt(data[1]));
					
					Thread.sleep(5);
					
					if(PongServerMain.state == State.PLAYING) {
						String answer = Proto.OBJECTS.n + "@" + mgr.getObjectsData();
						sendPacket(client, answer);
					} else {
						String answer = Proto.STOP_CONNEXION.n + "@1";
						sendPacket(client, answer);
					}
				}
				
				else if(protocol == Proto.READY) {
					if(PongServerMain.state == State.PLAYING) {
						String startData = Proto.GAME_START.n + "@" + mgr.getMapData();
						Thread.sleep(20);
						sendPacket(client, startData);
					} else {
						sendPacket(client, Proto.NO_READY.n + "@Game hasn't started yet !");
					}
				}
				
				else if(protocol == Proto.STOP_CONNEXION) {
					PongServerMain.leave(client);
				}
			}
			server.close();
			return;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void sendPacket(ClientInfo client, String data) {
		try {
			byte[] buffer = data.getBytes();
			DatagramPacket packet = new DatagramPacket(
					buffer,
					buffer.length,
					client.getAdress(),
					client.getPort()
			);
			server.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
