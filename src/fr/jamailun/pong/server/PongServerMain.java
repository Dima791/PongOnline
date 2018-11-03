package fr.jamailun.pong.server;

import java.io.IOException;
import java.net.ServerSocket;

import fr.jamailun.pong.values.PongLibrairies;

public class PongServerMain {
	
	static int ps1, ps2;
	static ClientInfo client1, client2;
	static GameDataManager mgr;
	private static Thread watcher;
	
	static State state;
	
	public static void main(String[] args) {
		print("[PongOnline] - version " + PongLibrairies.$ver);
		print("[PongOnline] - by jamailun");
		print("[PongOnline] - Copyright 2018-2019 - GNU3");
		
		state = State.WAINTING_PLAYERS;
		
		print("Initiate game data.");
		mgr = new GameDataManager();
		
		print("Starting server...");
		
		Thread server = new Thread(new PongServerDefault());
		server.start();
		
		ps1 = 0;
		init();
		
	}
	
	private static void init() {
		print("Booting...");
		state = State.WAINTING_PLAYERS;
		
		mgr.reboot();
		
		watcher = new Thread(new GameWatcher(mgr));
		
		if(ps1 != 0) {
			try {
				Runtime.getRuntime().exec("iptables -D INPUT -p udp --dport " + ps1 + " -j ACCEPT");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Runtime.getRuntime().exec("iptables -D INPUT -p udp --dport " + ps2 + " -j ACCEPT");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		print(" ");
		print("Allocating ports...");
		allocatingPorts();
		print("Ports allocated :");
		print("Slot 1 : [" + ps1 + "]");
		print("Slot 2 : [" + ps2 + "]");
		
		try {
			Runtime.getRuntime().exec("iptables -A INPUT -p udp --dport " + ps1 + " -j ACCEPT");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Runtime.getRuntime().exec("iptables -A INPUT -p udp --dport " + ps2 + " -j ACCEPT");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		print("Server started.");
	}
	
	public static void startGame() {
		if( ! isServerFull())
			return;
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {e.printStackTrace();}
		
		state = State.PLAYING;
		
		print("Game started !");
		
		watcher.start();
		
	}
	
	
	
	public static void leave(ClientInfo client) {
		if(state == State.PLAYING) {
			print("Client at ["+client.ip+"|"+client.port+"] leaved the game !");
			state = State.WAINTING_PLAYERS;
			print("Ending game. Restarting in 3 seconds.");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {e.printStackTrace();}
			init();
		}
	}
	
	public static boolean isConnected(ClientInfo client) {
		if(client1 != null)
			if(client.equals(client1))
				return true;
		if(client2 != null)
			if(client.equals(client2))
				return true;
		return false;
	}
	
	public static void connect(ClientInfo client) {
		if(client1 == null) {
			client1 = client;
			print("Client at ["+client.ip+"|"+client.port+"] connected (slot 1).");
			Thread c = new Thread(new ActiveServerListener(ps1, client1, mgr));
			c.start();
			return;
		}
		if(client2 == null) {
			client2 = client;
			print("Client at ["+client.ip+"|"+client.port+"] connected (slot 2).");
			Thread c = new Thread(new ActiveServerListener(ps2, client2, mgr));
			c.start();
			return;
		}
		System.err.println("[SERVER][ERROR] Client at ["+client.ip+"|"+client.port+"] failed to connect !");
	}
	
	public static void disconnect(ClientInfo client) {
		if(client1.equals(client))
			client1 = null;
		if(client2.equals(client))
			client2 = null;
		print("Client at ["+client.ip+"|"+client.port+"] disconnected.");
	}
	
	private static void print(String msg) {
		System.out.println("[SERVER][INFO] " + msg);
	}
	
	public enum State {
		WAINTING_PLAYERS,
		PLAYING,
		STOP;
	}

	public static boolean isServerFull() {
		if(client1 == null)
			return false;
		if(client2 == null)
			return false;
		return true;
	}
	
	private static void allocatingPorts() {
		if(ps1 != 0 && ps2 != 0)
			return;
		try {
			ServerSocket s1 = new ServerSocket(0);
			ServerSocket s2 = new ServerSocket(0);
			ps1 = s1.getLocalPort();
			ps2 = s2.getLocalPort();
			s1.close();
			s2.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
