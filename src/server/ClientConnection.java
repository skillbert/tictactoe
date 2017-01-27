package server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;

import AI.SimpleAI;
import common.AsyncSocket;
import common.DirectProtocol;
import common.Mark;
import common.Player;
import common.RemotePlayer;
import common.SessionState;
import common.SocketProtocol;
import httpServer.BasicHttpServer;
import httpServer.WebSocketProtocol;

public class ClientConnection {
	private String name;
	private SessionState state;
	private AsyncSocket sock;
	private Server server;
	private RemotePlayer player;
	private boolean isBasicSocket = true;
	private SocketProtocol socketProtocol;

	/**
	 * Initializes a ClientConnection
	 * 
	 * @param server
	 *            Server class to use
	 * @param asyncChannel
	 *            asyncChannel to use
	 */
	public ClientConnection(Server server, AsynchronousSocketChannel asyncChannel) {
		this.server = server;
		state = SessionState.authenticating;
		this.name = "";

		sock = new AsyncSocket(asyncChannel);
		sock.onClose(() -> server.disconnectClient(this));
		sock.onPacket(packet -> parsePacket(packet));

		socketProtocol = new DirectProtocol();
		socketProtocol.setSocket(sock);
	}

	/**
	 * Parses a raw packet from connection and passes it on to the correct
	 * module
	 * 
	 * @param packet
	 *            the packet to parse
	 */
	private void parsePacket(byte[] packet) {
		// check if the request is using a different protocol
		if (isBasicSocket && state == SessionState.authenticating) {
			// websocket
			WebSocketProtocol websock = WebSocketProtocol.tryConnectWebsocket(packet, sock);
			if (websock != null) {
				socketProtocol = websock;
				isBasicSocket = false;
				System.out.println("websocket client connected");
				return;
			}

			// simple http request
			if (BasicHttpServer.tryRespondGET(packet, sock)) {
				return;
			}
		}

		String message = socketProtocol.parsePacket(packet);
		if (message != null) {
			parseMessage(message);
		}
	}

	/**
	 * Parse a client message according to the protocol
	 * 
	 * @param message
	 *            client message to parse
	 */
	private void parseMessage(String message) {
		System.out.println(name + "\t>> " + message);
		String[] parts = message.split("\\s+");
		switch (parts[0]) {
		case "queue":
			queueGame();
			break;

		case "leaveQueue":
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
			login(parts[1]);
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
			if (state != SessionState.lobby && state != SessionState.queued) {
				showModalMessage("You need to be in the lobby to start a new game");
				break;
			}
			ArrayList<Player> players = new ArrayList<>();
			players.add(new RemotePlayer(this, Mark.RED));
			// TODO do something to make sure this name is available
			players.add(new SimpleAI("OK_Bot", Mark.YELLOW));
			server.startGame(players);
		}
	}

	/**
	 * leaves a game if the current SessionState is ingame, sets the
	 * SessionState to lobby if successful.
	 */
	private void leaveGame() {
		if (state != SessionState.ingame) {
			showModalMessage("You need to be in a game to leave one.");
			return;
		}
		setState(SessionState.lobby);
		setPlayer(null);
		// TODO tell our opponent about it, can't with protocol
	}

	/**
	 * Executes a move, next turn if successful.
	 * 
	 * @param row
	 *            chosen row
	 * @param col
	 *            chosen col
	 */
	private void commitMove(int row, int col) {
		if (state != SessionState.ingame) {
			sendString("error invalidMove");
			System.out.println("invalid move " + state);
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
	}

	/**
	 * Attempts to log in with the given name, will send errors if the name is
	 * not available or valid or if this connection can not log in at this
	 * moment
	 * 
	 * @param name
	 *            chosen name
	 */
	private void login(String name) {
		if (state != SessionState.authenticating) {
			showModalMessage("Already logged in. You can't set your name at this time");
			return;
		}

		if (!name.matches("^\\w+$")) {
			sendString("error invalidCharacters");
			return;
		}
		if (server.findPlayer(name) != null) {
			sendString("error nameTaken");
			return;
		}

		this.name = name;
		setState(SessionState.lobby);
		sendString("lobby");
	}

	/**
	 * disconnect by closing the this.sock
	 */
	public void disconnect() {
		sock.close();
		setState(SessionState.disconnected);
		// TODO leave game
	}

	/**
	 * getter name
	 * 
	 * @return this.name
	 */
	public String getName() {
		return name;
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
	 */
	public void setState(SessionState state) {
		this.state = state;
		server.broadcastPlayers();
	}

	/**
	 * Send an error message
	 * 
	 * @param message
	 *            error message to send.
	 */
	public void showModalMessage(String message) {
		sendString("error errorMessage " + message);
	}

	/**
	 * Send a String str trough the AsyncSocket this.sock
	 * 
	 * @param str
	 *            string to send trough this.sock
	 */
	public void sendString(String str) {
		System.out.println(name + "\t<< " + str);
		sock.sendPacket(socketProtocol.textPacket(str));
	}

	/**
	 * setter server Player
	 * 
	 * @param player
	 *            server Player object
	 */
	public void setPlayer(RemotePlayer player) {
		this.player = player;
	}

	/**
	 * getter server Player
	 * 
	 * @return this.player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * getter Server
	 * 
	 * @return this.server
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Gets whether this client connection is a player that is logged in
	 * 
	 * @return
	 */
	public boolean isLoggedIn() {
		return state == SessionState.authenticating || state == SessionState.queued || state == SessionState.ingame;
	}
}


























