package fr.jamailun.pong.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import fr.jamailun.pong.server.PongServerMain.State;
import fr.jamailun.pong.values.PongLibrairies;
import fr.jamailun.pong.values.Proto;

public class PongServerDefault implements Runnable {

	private DatagramSocket server;
	
	public PongServerDefault() {
		if(server == null) {
			try {
				server = new DatagramSocket(PongLibrairies.$publicServerPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			
			while(true) {
				
				Thread.sleep(5000);
				
				//réception packet
				byte[] buffer = new byte[PongLibrairies.$packetSize];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				server.receive(packet);
				
				//Traitement
				String dataGlobal = PongLibrairies.removeSpaces(packet.getData());
				
				String[] data = dataGlobal.split("@", 2);
				Proto protocol = Proto.getWithNumber(data[0]);
				
				System.out.println("[SERVER][HUB] Get packet from " + packet.getAddress().getHostAddress() +"|"+packet.getPort() +". Packet: "+dataGlobal);
				if(data.length < 2) {
					System.err.println("[SERVER][ERROR] This packet is not correct !");
					continue;
				}
				
				
				String rep = PongLibrairies.error(PongLibrairies.UNKNOW_ERROR);
				if(protocol == Proto.HANDSHAKE) {
					
					ClientInfo client = new ClientInfo(packet.getAddress(), Integer.parseInt(data[1]));
					
					if(PongServerMain.state == State.WAINTING_PLAYERS) {
						if(!PongServerMain.isServerFull()) {
							if(!PongServerMain.isConnected(client)) {
								PongServerMain.connect(client);
								if(client.equals(PongServerMain.client1))
									rep = "true@" + PongServerMain.ps1;
								else if(client.equals(PongServerMain.client2))
									rep = "true@" + PongServerMain.ps2;
								else
									System.err.println("[SERVER][ERROR] Client doesn't appear in data !");
								resendPacket(packet, rep);
								System.out.println("[SERVER][HUB] Sending : "+rep);
								Thread.sleep(200);
								
								PongServerMain.startGame();
							} else {
								rep = PongLibrairies.error(PongLibrairies.ALREADY_CONNECTED);
							}
						} else {
							rep = PongLibrairies.error(PongLibrairies.SERVER_FULL);
						}
					} else {
						rep = PongLibrairies.error(PongLibrairies.SERVER_FULL);
					}
				}
				
				if( ! rep.equals(PongLibrairies.UNKNOW_ERROR))
					resendPacket(packet, rep);
				
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resendPacket(DatagramPacket packetGot, String data) {
		try {
			byte[] buffer = data.getBytes();
			DatagramPacket packet = new DatagramPacket(
					buffer,
					buffer.length,
					packetGot.getAddress(),
					packetGot.getPort()
			);
			
			server.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
