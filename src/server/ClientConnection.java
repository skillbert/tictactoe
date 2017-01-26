package server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;

import AI.SimpleAI;
import common.AsyncSocket;
import common.Mark;
import common.Player;
import common.Protocol;
import common.RemotePlayer;
import common.SessionState;
import httpServer.BasicHttpServer;
import httpServer.WebSocketProtocol;

public class ClientConnection {
	private String name;
	private SessionState state;
	private AsyncSocket sock;
	private Server server;
	private RemotePlayer player;
	private boolean isBasicSocket = true;

	/**
	 * Initializes a ClientConnection
	 * @param server
	 *             Server class to use
	 * @param asyncChannel
	 *             asyncChannel to use
	 */
	public ClientConnection(Server server, AsynchronousSocketChannel asyncChannel) {
		this.server = server;
		this.state = SessionState.authenticating;
		this.name = "";

		setSocket(new AsyncSocket(asyncChannel));
	}

	/**
	 * Initializes AsyncSocket 
	 * @param sock
	 *             socket to initialize
	 */
	private void setSocket(AsyncSocket sock) {
		this.sock = sock;
		sock.onClose(() -> server.disconnectClient(this));
		sock.onMessage(str -> parseMessage(str));
	}

	/**
	 * Parse a client message according to the protocol
	 * @param message
	 *             client message to parse
	 */
	private void parseMessage(String message) {
		// check if the request is using a different protocol
		if (isBasicSocket && state == SessionState.authenticating) {
			// websocket
			WebSocketProtocol websock = WebSocketProtocol.tryConnectWebsocket(message, sock);
			if (websock != null) {
				sock.setProtocol(websock);
				isBasicSocket = false;
				System.out.println("websocket client connected");
				return;
			}

			// simple http request
			if (BasicHttpServer.tryRespondGET(message, sock)) {
				return;
			}
		}

		System.out.println(name + "\t>> " + message);
		String[] parts = message.split(" ");
		switch (parts[0]) {
		case "queue":
			queueGame();
			break;

		case "unqueue":
			unQueueGame();
			break;

		case "leaveGame":
			leaveGame();
			break;

		case "login":
			if (parts.length < 2) {
				sendString("error errorMessage No name specified");
				break;
			}
			setName(parts[1]);
			break;

		case "place":
			if (parts.length < 3) {
				sendString("error invalidMove");
				System.out.println("invalid move 1");
				break;
			}
			int x, y;
			try {
				x = Integer.parseUnsignedInt(parts[1]);
				y = Integer.parseUnsignedInt(parts[2]);
			} catch (NumberFormatException ex) {
				sendString("error invalidMove");
				System.out.println("invalid move 2");
				break;
			}
			commitMove(x, y);
			break;

		case "bot":
			ArrayList<Player> players = new ArrayList<>();
			players.add(new RemotePlayer(this, Mark.RED));
			players.add(new SimpleAI("OK_Bot", Mark.YELLOW));
			server.startGame(players);

		}
	}

	/**
	 * leaves a game if the current SessionState is ingame, sets the SessionState to lobby if successful.
	 */
	private void leaveGame() {
		if (state != SessionState.ingame) {
			showModalMessage("You need to be in a game to leave one.");
			return;
		}
		state = SessionState.lobby;
		setPlayer(null);
		sendString("lobby");
		// TODO tell our opponent about it, can't with protocol
	}

	/**
	 * Executes a move, next turn if successful.
	 * @param row 
	 *             chosen row
	 * @param col
	 *             chosen col
	 */
	private void commitMove(int row, int col) {
		if (state != SessionState.ingame) {
			sendString("error invalidMove");
			System.out.println("invalid move 3 " + state);
			return;
		}
		player.getGame().commitMove(player, col, row);
	}

	/**
	 * Sets the SessionState to queued if the current SessionState is lobby.
	 * Calls server.findQueue() if succesful.
	 */
	public void queueGame() {
		if (state != SessionState.lobby) {
			showModalMessage("You need to be in the lobby to queue");
			return;
		}
		state = SessionState.queued;
		sendString("waiting");
		server.findQueue();
	}

	/**
	 * Sets the SessionState to lobby if the current SessionState is queued.
	 */
	public void unQueueGame() {
		if (state != SessionState.queued) {
			showModalMessage("You need to be queued in order to leave the queue");
			return;
		}
		state = SessionState.lobby;
		// TODO non-standard protocol
		sendString("lobby");
	}

	/**
	 * sets the player name if SessionState is authenticating, the name doens't contain invalid characters
	 * the name is not already taken. If it's successful it sets SessionState to lobby and queues automatically for a game. 
	 * @param name
	 *             chosen name
	 */
	private void setName(String name) {
		if (state != SessionState.authenticating) {
			showModalMessage("Already logged in. You can't set your name at this time");
			return;
		}
		// TODO the protocol doesn't actually specify a legal character, "word"
		// characters is assumed here. \w matches only a-zA-Z0-9_
		if (!name.matches("\\w")) {
			sendString("error invalidCharacters");
			return;
		}
		if (server.findPlayer(name) != null) {
			sendString("error nameTaken");
			return;
		}

		this.name = name;
		state = SessionState.lobby;

		if (Protocol.followStandards) {
			// the standard requires us to instantly join the queue
			queueGame();
		} else {
			// but we really just want to go to the lobby and decide there
			sendString("lobby");
		}
	}

	/**
	 * disconnect by closing the this.sock
	 */
	public void disconnect() {
		sock.close();
	}

	/** 
	 * getter name
	 * @return this.name
	 */
	public String getName() {
		return name;
	}

	/**
	 * getter state
	 * @return this.state 
	 */
	public SessionState getState() {
		return state;
	}

	/**
	 * setter this.state
	 * @param state
	 */
	public void setState(SessionState state) {
		this.state = state;
	}

	/**
	 * Send an error message
	 * @param message 
	 *             error message to send.
	 */
	public void showModalMessage(String message) {
		sendString("error errorMessage " + message);
	}

	/**
	 * Send a String str trough the AsyncSocket this.sock
	 * @param str string
	 *             to send trough this.sock
	 */
	public void sendString(String str) {
		System.out.println(name + "\t<< " + str);
		sock.sendString(str);
	}

	/**
	 * setter server Player
	 * @param player
	 *             server Player object
	 */
	public void setPlayer(RemotePlayer player) {
		this.player = player;
	}

	/**
	 * getter server Player
	 * @return this.player
	 */
	public Player getPlayer() {
		return player;
	}
}


























