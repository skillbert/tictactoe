package common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Protocol {
	public static final int DEFAULTPORT = 12345;
	public static final Charset charset = StandardCharsets.UTF_8;
	
	public static final String DELIMITER = " ";
	public static final String QUEUE = "queue";
	public static final String LEAVEQUEUE = "leaveQueue";
	public static final String WAITING = "waiting";
	public static final String LOGIN = "login";
	public static final String PLACED = "placed";
	public static final String PLACE = "place";
	public static final String PLAYERS = "players";
	public static final String ERROR = "error";
	public static final String STARTGAME = "startGame";

	// unofficial
	public static final String LOBBY = "lobby";
	public static final String BOT = "bot";
	public static final String LEAVEGAME = "leaveGame";
	public static final String ERROR_INVALIDCOMMAND = "error invalidCommand";
	public static final String UNKNOWNCOMMAND = "error unknownCommand";
	public static final int DIM = 4;
}
