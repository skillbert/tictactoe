package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


import client.gui.Gui;
import common.AsyncSocket;
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
	private String myName;
	private SocketProtocol protocol;
	private Map<String, String> playerLobbyData = new HashMap<String, String>();
	
	/**
	 * Initializes a new Session by creating a new Peerplayer (network player)
	 * and initializing a UI.
	 */
	public Session() {
		setState(SessionState.disconnected);
		myName = "";
		ui = new Gui(this);
		this.addObserver(ui);
	}
	
	/**
	 * calls run() on the ui so starts interaction.
	 */
	public void run() {
		ui.run();
	}
	
	/**
	 * getter state
	 * 
	 * @return this.state
	 */
	public SessionState getState() {
		return state;
	}
	
	/**
	 * setter this.state
	 * 
	 * @param state
	 *            state to set.
	 */
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
	public void commitMove(int x, int y) {
		sendMessage(Protocol.PLACE + Protocol.DELIMITER + x + Protocol.DELIMITER + y);
	}
	
	/**
	 * Logs in with the chosen name
	 * 
	 * @param name
	 *            chosen name
	 */
	public void login(String name) {
		myName = name;
		sendMessage(Protocol.LOGIN + Protocol.DELIMITER + name);
	}
	
	/**
	 * Queues for a game if the current SessionState is lobby.
	 */
	public void queueGame() {
		if (state != SessionState.lobby) {
			ui.showModalMessage("You need to be in the lobby to queue for a game.");
			return;
		}
		
		sendMessage(Protocol.QUEUE);
	}
	
	/**
	 * Cancels queue for a game if the current SessionState is queued.
	 */
	public void cancelQueueGame() {
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
	private void sendMessage(String message) {
		sock.sendPacket(protocol.textPacket(message));
	}
	
	/**
	 * Connection closed handler, sets SessionState to disconnected and sock to
	 * null.
	 */
	private void connectionClosed() {
		setState(SessionState.disconnected);
		sock = null;
	}
	
	/**
	 * Connection failed handler, sets SessionState to disconnected and shows a
	 * message to the user indicating the failure
	 */
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
		
		for (String message:messages.split("\n")){
			CommandParser command = new CommandParser(message);
			System.out.println("server msg > " + message + "<");
			
			try {
				switch (command.getCommand()) {
					case Protocol.ERROR:
						parseError(command.nextString(), command.remainingString());
						break;
					
					case Protocol.STARTGAME:
						startGame(command.nextString(), command.nextString());
						break;
					
					case Protocol.WAITING:
						setState(SessionState.queued);
						break;
					
					case Protocol.LOBBY:
						setState(SessionState.lobby);
						break;
					
					case Protocol.PLAYERS:
						System.out.println("PLAYERS");
						updatePlayerLobbyData(command.remainingString());
						break;
						
					case Protocol.PLACED:
						parsePlaced(command.nextString(), command.nextInt(), command.nextInt(),
								command.nextString(), command.nextString());
						break;
					
					default:
						// TODO ignore this or do something else?
						System.out.println("unknown command from server");
				}
			} catch (CommandFormatException ex) {
				System.out.println("Invalid command received from server");
			}
		}
	}
	
	private void updatePlayerLobbyData(String playerStr){
		System.out.println(playerStr);
		playerLobbyData = new HashMap<String, String>();
		String[] playerStates = playerStr.split(" ");
		for (String playerState:playerStates) {
			String[] ps = playerState.split("-");
			playerLobbyData.put(ps[0], ps[1]);
		}
		System.out.println("Notifiying lobby");
		setChanged();
		notifyObservers(Ui.UpdateType.lobby);
	}
	
	public Map<String, String> getPlayerLobbyData() {
		return this.playerLobbyData;
	}
	
	/**
	 * parse the new move message, verify the player that made the move, and
	 * make the move in the client.
	 * 
	 * @param parts
	 */
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
	private void startGame(String playername1, String playername2) {
		ArrayList<Player> players = new ArrayList<>();
		players.add(new PeerPlayer(playername1, 0));
		players.add(new PeerPlayer(playername2, 1));
		currentGame = new Game(players);
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
	private void parseError(String type, String message) {
		// TODO put these strings in nice constants
		switch (type) {
			case "errorMessage":
				ui.showModalMessage(message);
				break;
			case "nameTaken":
				ui.showModalMessage("This name is already taken.");
				break;
			case "invalidCharacters":
				ui.showModalMessage("The name you chose contains invalid characters.");
				break;
			case "couldNotStart":
				// TODO this error is specified in the protocol, does it
				// actually
				// exist?
				ui.showModalMessage("Error: Could not start");
				break;
			case "invalidMove":
				ui.showModalMessage("Invalid move");
				// TODO rewind the game state and find out how this error could
				// happen
				break;
		}
	}
	
	/**
	 * getter currentGame
	 * 
	 * @return this.currentGame
	 */
	public Game getGame() {
		return currentGame;
	}
	
	/**
	 * Sets SessionState to authenticating
	 */
	private void connected() {
		setState(SessionState.authenticating);
	}
	
	/**
	 * Called when the server sends something unexpected
	 */
	private void UnknownServerError(String reason) {
		// TODO figure out what to actually do with this, do we
		// disconnect/throw/ingore?
		System.out.println("Server protocol error: " + reason);
	}
	
}
















