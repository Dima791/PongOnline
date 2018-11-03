package fr.jamailun.pong.values;

public enum Proto {

	/**
	 * 		PROTOCOL_NUMBER @ DATA_1 $ DATA_2 $ ... $ DATA_n
	 */

	NOT_A_PROTOCOL(-1, "not a protocol"),
	
	
	HANDSHAKE(0, "handshake"),
	STOP_CONNEXION(1, "close connexion"),
	KEEP_ALIVE(2, "keep_alive"),
	FATAL_ERROR(666, "Fatal error, system failure."),
	
	/** @SERVEUR -/-> @CLIENT**/
	GAME_START(10, "Game start"), //avec: map_height, map_width, tile_height, tile_width, ball_size
	OBJECTS(11, "objects"), //avec: tile1_x, tile1_y, tile2_x, tile2_y, ball_x, ball_y, scoreL, scoreR
	NO_READY(12, "No ready yet !"),
	
	/** @CLIENT ---> @SERVEUR**/
	READY(20, "ready?"), //demande au serveur si on peut commencer !
	MOOVE_TILE(21, "moove tile"), //avec INTEGER (de combien ça a été déplacé pendant les 2 packets)
	
	;
	
	public int n;
	public String title;
	
	private Proto(int n, String title) {
		this.n= n;
		this.title = title;
	}
	
	public static Proto getWithNumber(int n) {
		for(Proto p : values())
			if(p.n == n)
				return p;
		return Proto.NOT_A_PROTOCOL;
	}
	
	public static Proto getWithNumber(String n) {
		try {
			for(Proto p : values())
				if(p.n == Integer.parseInt(n))
					return p;
			return Proto.NOT_A_PROTOCOL;
		} catch (Exception e) {
			return Proto.NOT_A_PROTOCOL;
		}
	}
	
	/*
	 * Alors ça tourne en continu, le client envoie régulièrement des "ping" au server, 
	 * qui sont remplacé par des clienèserver si il envoit des infos au serveur. 
	 * 
	 * Comme ça, si le serveur ne recoit pas d'in depuis trop logntemps, 
	 * on peut penser que le connaction du client a lachée !
	 */
	
}
