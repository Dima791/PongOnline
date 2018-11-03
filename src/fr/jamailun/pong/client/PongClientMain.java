package fr.jamailun.pong.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;

import javax.swing.JFrame;

import fr.jamailun.pong.miscellaneous.PictureLoader;
import fr.jamailun.pong.values.PongLibrairies;

public class PongClientMain {
	
	static boolean closedS = false, closedL = false;

	static State state;
	static PongPanel panel;
	static int pc = 0;
	static int ps = 0;
	
	static Thread boot, clientSender;
	
	public static void main(String[] args) {
		state = State.BOOT;
		
		print("Opening window...");
		JFrame window = new JFrame("PongOnline  |  ver." + PongLibrairies.$ver + "  |  by jamailun");
		panel = new PongPanel(window);
		window.setContentPane(panel);
		try {
			window.setIconImage((PictureLoader.getImage("src/fr/jamailun/pong/miscellaneous/img-client.png")));
		} catch(Exception e) {}
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				print("Stopping procress...");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {e1.printStackTrace();}
				state = State.STOPING;
				print("All process closed correctly. Closing program.");
				window.dispose();
				System.exit(0);
			}
		});
		window.setResizable(false);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		print("Window opened");
		
		init();
		
	}
	
	public static void resendHandshake() {
		if(state == State.BOOT) {
			boot = new Thread(new BootingClient(panel));
			boot.start();
		}
	}
	
	private static void init() {
		print("Allocating ports");
		allocatingPorts();
		print("Port allocated. Client->Server:[" + pc + "]");
		
		print("Starting boot client...");
		boot = new Thread(new BootingClient(panel));
		boot.start();
		
		print("Boot client started.");
	}
	
	/**
	 * @param portServer1 : Client -> Server
	 */
	//@SuppressWarnings("deprecation")
	public static void connected(int portServer) {
		print("Connected to the server at port ["+portServer+"] !");
		
		ps = portServer;
		state = State.WAITING_GAME;
		
		clientSender = new Thread(new ClientSender(panel, pc, ps));
		clientSender.start();
		
		print("Initialisation is over !");
	}
	
	public static void print(String msg) {
		System.out.println("[CLIENT][INIT] " + msg);
	}

	private static void allocatingPorts() {
		if(pc != 0)
			return;
		try {
			ServerSocket s1 = new ServerSocket(0);
			pc = s1.getLocalPort();
			s1.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public enum State {
		BOOT,
		WAITING_GAME,
		PLAYING,
		STOPING;
	}

	public static boolean isOver() {
		if(state == State.STOPING)
			return true;
		return false;
	}

	public static void deconnect() {
		System.err.println("[CLIENT][WARN] The server disconnected us.");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {e1.printStackTrace();}
		state = State.STOPING;
		
		panel.deconnect();
		print("Restarting process in 3 seconds.");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {e1.printStackTrace();}
		
		init();
	}

	public static void timeoutOnSender() {
		clientSender = new Thread(new ClientSender(panel, pc, ps));
		clientSender.start();
	}
	
}
