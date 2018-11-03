package fr.jamailun.pong.server;

import java.net.InetAddress;

import fr.jamailun.pong.server.PongServerMain.State;

public class ClientInfo {

	public InetAddress ip;
	public int port;
	public State state;
	
	public long lastKeepAlive;
	
	public ClientInfo(InetAddress adress, int port) {
		this.ip = adress;
		this.port = port;
		this.state = State.STOP;
		keepAlive();
	}
	
	public void keepAlive() {
		lastKeepAlive = System.currentTimeMillis();
	}
	
	public long getLastKeepAlive() {
		return lastKeepAlive;
	}
	
	public InetAddress getAdress() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean equals(ClientInfo client) {
		if(client.ip.toString().equals(ip.toString()))
			if(client.port == port)
					return true;
		return false;
	}
	
}
