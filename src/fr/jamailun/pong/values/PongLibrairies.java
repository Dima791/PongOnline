package fr.jamailun.pong.values;

public class PongLibrairies {
	
	/**
	 * 		@ERRORS
	 */
	
	public final static String UNKNOW_ERROR = "Unknow error.";
	
	public final static String SERVER_FULL = "Server full.";
	public final static String ALREADY_CONNECTED = "Already connected to the server.";
	public final static String NOT_CONNECTED = "Not connected to the server.";
	
	
	public static String error(String str) {
		return "ERROR: " + str;
	}
	
	/**
	 * 		@DATA
	 */
	
	public final static String $ver = "BETA-release";
	
	public final static int $packetSize = 256;
	
	public final static String $publicServerIp = "149.91.82.85";
	public final static int $publicServerPort = 140;
	
	/**
	 * @AUTRE
	 */
	
	public static String removeSpaces(byte[] bytes) {
		String str = "";
		for(byte bit : bytes)
			if(bit != 0)
				str = str + (new String(new byte[] {bit}));
		return str;
	}
}
