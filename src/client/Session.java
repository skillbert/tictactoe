package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;

import client.Ui.UpdateType;
import client.gui.Gui;
import common.AsyncSocket;
import common.Board;
import common.CommandParser;
import common.CommandParser.CommandFormatException;
import common.DirectProtocol;
import common.Game;
import common.Game.GameState;
import common.Player;
import common.Protocol;
import common.SessionState;
import common.SocketProtocol;

public class Session extends Observable {
	
	private AsyncSocket sock;
	private SessionState state;
	private Game currentGame;
	private Ui ui;
	private SocketProtocol protocol;
	private Map<String, String> playerLobbyData = new HashMap<String, String>();
	private GameInvitation invitation;
	
	/**
	 * Initializes a new Session by creating a new Peerplayer (network player)
	 * and initializing a UI.
	 */
	//@ ensures getState() == SessionState.disconnected;
	public Session() {
		setState(SessionState.disconnected);
		ui = new Gui(this);
		this.addObserver(ui);
	}
	
	/**
	 * calls run() on the ui so starts interaction.
	 */
	/*@ pure */ public void run() {
		ui.run();
	}
	
	/**
	 * getter state
	 * 
	 * @return this.state
	 */
	/*@ pure */ public SessionState getState() {
		return state;
	}
	
	/**
	 * setter this.state
	 * 
	 * @param state
	 *            state to set.
	 */
	//@ ensures getState() == state;
	public void setState(SessionState state) {
		this.state = state;
		setChanged();
		notifyObservers(Ui.UpdateType.state);
	}
	
	/**
	 * Sets up a connection to host:port using an AcynSocket()
	 * 
	 * @param host
	 *            Host to connect to
	 * @param port
	 *            Port to connect to
	 */
	//@ ensures getState() == SessionState.authenticating;
	public void connect(String host, int port) {
		setState(SessionState.connecting);
		sock = new AsyncSocket();
		sock.onClose(() -> connectionClosed());
		sock.onPacket(packet -> parseMessage(packet));
		sock.onConnect(() -> connected());
		sock.onConnectFail(() -> connectFailed());
		protocol = new DirectProtocol();
		protocol.setSocket(sock);
		try {
			sock.connect(host, port);
		} catch (IOException ex) {
			System.out.println("Failed to connect, error: " + ex.getMessage());
			return;
		}
	}
	
	/**
	 * Commits a chosen move by sending it over the socket to the server.
	 * 
	 * @param x
	 *            chosen x / column
	 * @param y
	 *            chosen y / row
	 */
	/* @ pure */  public void commitMove(int x, int y) {
		sendMessage(Protocol.PLACE + Protocol.DELIMITER + x + Protocol.DELIMITER + y);
	}
	
	/**
	 * Logs in with the chosen name
	 * 
	 * @param name
	 *            chosen name
	 */
	/* @ pure */  public void login(String name) {
		sendMessage(Protocol.LOGIN + Protocol.DELIMITER + name);
	}
	
	/**
	 * sends an invite for a custom game with given board size to the specified
	 * players
	 * 
	 * @param boardSize
	 *            the size of the custom game board
	 * @param players
	 *            a list of player names to invite
	 */
	//@requires getState() == SessionState.lobby;
	//@ requires players.size() > 0;
	public void invite(int boardSize, ArrayList<String> players) {
		if (state != SessionState.lobby) {
			ui.showModalMessage("You need to be in the lobby to invite players");
			return;
		}
		if (players.size() == 0) {
			ui.showModalMessage("You need to invite at least one player");
			return;
		}
		
		String playerstr = "";
		for (String player : players) {
			playerstr += " " + player;
		}
		
		setState(SessionState.invited);
		sendMessage(Protocol.INVITE + " " + boardSize + playerstr);
	}
	
	/**
	 * Replies to an invite
	 * 
	 * @param accept
	 *            true is accepted, false if the invite is rejected
	 */
	//@ requires getState() == SessionState.invited;
	public void replyInvite(boolean accept) {
		if (state != SessionState.invited) {
			ui.showModalMessage("You don't have any invitations");
			return;
		}
		if (!accept) {
			setState(SessionState.lobby);
		}
		
		sendMessage(Protocol.REPLY + " " + (accept ? "yes" : "no"));
	}
	
	/**
	 * Queues for a game if the current SessionState is lobby.
	 */
	//@ requires getState() == SessionState.lobby;
	/* @ pure */ public void queueGame() {
		if (state != SessionState.lobby) {
			ui.showModalMessage("You need to be in the lobby to queue for a game.");
			return;
		}
		
		sendMessage(Protocol.QUEUE);
	}
	
	/**
	 * Cancels queue for a game if the current SessionState is queued.
	 */
	/* @ pure */ public void cancelQueueGame() {
		if (state != SessionState.queued) {
			ui.showModalMessage("You need to be in the lobby to queue for a game.");
			return;
		}
		
		sendMessage(Protocol.LEAVEQUEUE);
	}
	
	/**
	 * Sends a string message over the socket according to the protocol.
	 * 
	 * @param message
	 */
	/* @ pure */ private void sendMessage(String message) {
		sock.sendPacket(protocol.textPacket(message));
	}
	
	/**
	 * Connection closed handler, sets SessionState to disconnected and sock to
	 * null.
	 */
	//@ ensures getState() == SessionState.disconnected;
	private void connectionClosed() {
		setState(SessionState.disconnected);
		sock = null;
	}
	
	/**
	 * Connection failed handler, sets SessionState to disconnected and shows a
	 * message to the user indicating the failure
	 */
	//@ ensures getState() == SessionState.disconnected;
	private void connectFailed() {
		ui.showModalMessage("Failed to connect to the server");
		setState(SessionState.disconnected);
	}
	
	/**
	 * parse a message from the server
	 * 
	 * @param packet
	 */
	private void parseMessage(byte[] packet) {
		String messages = protocol.parsePacket(packet);
		if (messages == null) {
			return;
		}
		
		for (String message : messages.split("\n")) {
			CommandParser command = new CommandParser(message);
			
			try {
				switch (command.getCommand()) {
					case Protocol.ERROR:
						parseError(command.nextString(), command.remainingString());
						break;
					
					case Protocol.STARTGAME:
						startGame(Board.DEFAULTSIZE, command.remainingArguments());
						break;
					
					case Protocol.STARTCUSTOMGAME:
						startGame(command.nextInt(), command.remainingArguments());
						break;
					
					case Protocol.WAITING:
						setState(SessionState.queued);
						break;
					
					case Protocol.LOBBY:
						setState(SessionState.lobby);
						break;
					
					case Protocol.PLAYERS:
						updatePlayerLobbyData(command.remainingString());
						break;
					
					case Protocol.PLACED:
						parsePlaced(command.nextString(), command.nextInt(), command.nextInt(),
								command.nextString(), command.nextString());
						break;
					
					case Protocol.INVITATION:
						parseInvitation(command.nextInt(), command.nextString());
						break;
				}
			} catch (CommandFormatException ex) {
				ui.showModalMessage(ex.getMessage());
			}
		}
	}
	
	/**
	 * Handles new invitations from the server
	 * 
	 * @param boardSize
	 *            The size of the board
	 * @param inviter
	 *            The player that sent the invitation
	 */
	//@ requires boardSize > 0;
	//@ requires inviter != "";
	//@ ensures getState() == SessionState.invited;
	private void parseInvitation(int boardSize, String inviter) {
		invitation = new GameInvitation(boardSize, inviter);
		setState(SessionState.invited);
		setChanged();
		notifyObservers(UpdateType.state);
	}
	
	/**
	 * Updates the ui to reflect the current players on the server
	 * 
	 * @param playerStr
	 *            the raw player list string received from the server
	 */
	//@ requires playerStr.split(" ").length == 0 || playerStr.split(" ")[0].split("-").length == 2;
	//@ ensures (\forall int i; 0 <= i & i < playerStr.split(" ").length; getPlayerLobbyData().containsKey(playerStr.split(" ")[i].split("-")[0]));
	private void updatePlayerLobbyData(String playerStr) {
		playerLobbyData = new HashMap<String, String>();
		String[] playerStates = playerStr.split(" ");
		for (String playerState : playerStates) {
			String[] ps = playerState.split("-");
			playerLobbyData.put(ps[0], ps[1]);
		}
		setChanged();
		notifyObservers(Ui.UpdateType.lobby);
	}
	
	/**
	 * getter playerPlayerLobbyData
	 * @return Map of <String playername, String status>
	 */
	/*@ pure */  public Map<String, String> getPlayerLobbyData() {
		return this.playerLobbyData;
	}
	
	/**
	 * parse the new move message, verify the player that made the move, and
	 * make the move in the client.
	 * 
	 * @param parts
	 */
	//@ requires getState() == SessionState.ingame;
	private void parsePlaced(String gamestate, int x, int y, String currentPlayer,
			String nextPlayer) {
		if (state != SessionState.ingame) {
			UnknownServerError("Received a placed message while not in a game");
			return;
		}

		Optional<? extends Player> player = currentGame.getPlayers().stream()
				.filter(p -> p.getName().equals(currentPlayer)).findAny();
		if (!player.isPresent()) {
			UnknownServerError("Received a move from a player that is not in the current game");
			return;
		}
		
		currentGame.commitMove(player.get(), y, x);
		setChanged();
		notifyObservers(Ui.UpdateType.gamemove);
		if (!gamestate.equals(GameState.onGoing.toString())) {
			setState(SessionState.lobby);
		}
	}
	
	/**
	 * starts a game with 2 players by creating the PeerPlayer and Game objects,
	 * calling startGame() and setting the state to ingame
	 * 
	 * @param playername1
	 * @param playername2
	 */
	//@ requires (\forall int i; 0 <= i & i < playernames.length; !(playernames[i] == "" || playernames[i] == null));
	private void startGame(int boardSize, String[] playernames) {
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < playernames.length; i++) {
			players.add(new PeerPlayer(playernames[i], i));
		}
		currentGame = new Game(boardSize, players);
		currentGame.startGame();
		setState(SessionState.ingame);
	}
	
	/**
	 * Parsing error handler. Takes an error message and presents it to the
	 * user.
	 * 
	 * @param errorMessage
	 *            Error message to display.
	 */
	/*@ pure */  private void parseError(String type, String message) {
		// TODO put these strings in nice constants
		switch (type) {
			case Protocol.E_MESSAGE:
				ui.showModalMessage(message);
				break;
			case Protocol.E_NAMETAKEN:
				ui.showModalMessage("This name is already taken.");
				break;
			case Protocol.E_INVALIDNAME:
				ui.showModalMessage("The name you chose contains invalid characters.");
				break;
			case Protocol.E_COULDNOTSTART:
				// TODO this error is specified in the protocol, does it
				// actually
				// exist?
				ui.showModalMessage("Error: Could not start");
				break;
			case Protocol.E_INVALIDMOVE:
				ui.showModalMessage("Invalid move");
				// TODO rewind the game state and find out how this error could
				// happen
				break;
			case Protocol.E_INVITATIONDENIED:
				ui.showModalMessage(message.split(" ")[0] + " rejected the party invitation.");
				setState(SessionState.lobby);
				break;
			default:
				ui.showModalMessage("unknown error from server \"" + type + ": " + message + "\"");
				break;
		}
	}
	
	/**
	 * getter currentGame
	 * 
	 * @return this.currentGame
	 */
	/*@ pure */  public Game getGame() {
		return currentGame;
	}
	
	/**
	 * Sets SessionState to authenticating
	 */
	//@ ensures getState() == SessionState.authenticating;
	private void connected() {
		setState(SessionState.authenticating);
	}
	
	/**
	 * Called when the server sends something that is not according to the
	 * protocol
	 */
	/*@ pure */ private void UnknownServerError(String reason) {
		// TODO figure out what to actually do with this, do we
		// disconnect/throw/ingore?
		System.out.println("Server protocol error: " + reason);
	}
	
	/**
	 * Main method, only initializes a new Session() and calls run() on it to start the game.
	 * @param args
	 */
	public static void main(String[] args) {
		Session session = new Session();
		session.run();
	}
	
}
















