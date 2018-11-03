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
	
	public final static String $ver = "1.0.1";
	
	public final static int $packetSize = 256;
	
	public final static String $publicServerIp = "XXXXXXXXXX";
	public final static int $publicServerPort = XXXXXXXXXXXX;
	
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
