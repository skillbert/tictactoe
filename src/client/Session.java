package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Optional;

import common.AsyncSocket;
import common.DirectProtocol;
import common.Game;
import common.Game.GameState;
import common.Player;
import common.SessionState;
import common.SocketProtocol;

public class Session extends Observable {

	private AsyncSocket sock;
	private SessionState state;
	private Game currentGame;
	private Ui ui;
	private String myName;
	private SocketProtocol protocol;

	/**
	 * Initializes a new Session by creating a new Peerplayer (network player)
	 * and initializing a UI.
	 */
	public Session() {
		setState(SessionState.disconnected);
		myName = "";
		ui = new Tui(this);
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
		sendMessage("place " + x + " " + y);
	}

	/**
	 * Logs in with the chosen name
	 * 
	 * @param name
	 *            chosen name
	 */
	public void login(String name) {
		myName = name;
		sendMessage("login " + name);
	}

	/**
	 * Queues for a game if the current SessionState is lobby.
	 */
	public void queueGame() {
		if (state != SessionState.lobby) {
			ui.showModalMessage("You need to be in the lobby to queue for a game.");
			return;
		}

		sendMessage("queue");
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
		String message = protocol.parsePacket(packet);
		if (message == null) {
			return;
		}
		// TODO add some sort of protocol error handling if the server doesn't
		// behave
		String[] parts = message.split(" ");
		switch (parts[0]) {
		case "error":
			parseError(message.split(" ", 2)[1]);
			break;

		case "startGame":
			startGame(parts[1], parts[2]);
			break;

		case "waiting":
			setState(SessionState.queued);
			break;

		case "lobby":
			setState(SessionState.lobby);
			break;

		case "placed":
			parsePlaced(parts);
			break;
		}
	}

	/**
	 * parse the new move message, verify the player that made the move, and
	 * make the move in the client.
	 * 
	 * @param parts
	 */
	private void parsePlaced(String[] parts) {
		if (parts.length < 6) {
			throw new RuntimeException();
		}
		if (state != SessionState.ingame) {
			throw new RuntimeException();
		}

		String gamestate = parts[1];

		int x, y;
		try {
			x = Integer.parseUnsignedInt(parts[2]);
			y = Integer.parseUnsignedInt(parts[3]);
		} catch (NumberFormatException ex) {
			throw new RuntimeException();
		}

		String currentPlayer = parts[4];
		Optional<? extends Player> player = currentGame.getPlayers().stream()
				.filter(p -> p.getName().equals(currentPlayer)).findAny();

		if (!player.isPresent()) {
			throw new RuntimeException();
		}

		currentGame.commitMove(player.get(), y, x);
		setChanged();
		notifyObservers(Ui.UpdateType.gamemove);


		if (gamestate != GameState.onGoing.toString()) {
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
	private void parseError(String errorMessage) {
		String[] parts = errorMessage.split(" ", 2);
		switch (parts[0]) {
		case "errorMessage":
			ui.showModalMessage(parts[1]);
			break;
		case "nameTaken":
			ui.showModalMessage("This name is already taken.");
			break;
		case "invalidCharacters":
			ui.showModalMessage("The name you chose contains invalid characters.");
			break;
		case "couldNotStart":
			// TODO this error is specified in the protocol, does it actually
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
}
















