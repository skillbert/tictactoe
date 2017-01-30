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
	public static final String INVITE = "invite";
	public static final String REPLY = "reply";
	public static final String INVITATION = "invitation";
	
	// start of a game
	// startGame <player1> <player2>
	public static final String STARTGAME = "startGame";
	// gameStart <boardSize> [playerx]{2+}
	public static final String STARTCUSTOMGAME = "gameStart";
	
	// error types
	public static final String E_INVITATIONDENIED = "invitationDenied";
	public static final String E_MESSAGE = "errorMessage";
	public static final String E_NAMETAKEN = "nameTaken";
	public static final String E_INVALIDNAME = "invalidCharacters";
	public static final String E_COULDNOTSTART = "couldNotStart";
	public static final String E_INVALIDMOVE = "invalidMove";
	
	
	// unofficial
	public static final String LOBBY = "lobby";
	public static final String BOT = "bot";
	public static final String LEAVEGAME = "leaveGame";
	public static final String E_INVALIDCOMMAND = "invalidCommand";
	public static final String E_UNKNOWNCOMMAND = "unknownCommand";
	public static final int DIM = 4;
}
