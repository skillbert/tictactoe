package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import common.Game;
import common.Game.EventType;
import common.Game.GameState;
import common.Player;
import common.Protocol;
import common.RemotePlayer;
import common.SessionState;
import common.Util;

public class Server implements Observer {
	private AsynchronousServerSocketChannel ssocket;
	private ArrayList<ClientConnection> clients;
	private ArrayList<Game> activeGames;

	public static void main(String[] args) throws IOException {
		try {
			new Server(Protocol.DEFAULTPORT);
		} catch (IOException ex) {
			System.out.println("Server error " + ex.getMessage());
		}
		// keeps the server running until some user input
		System.in.read();
	}

	/**
	 * Creates a new game server and starts listening on the given port
	 * 
	 * @param port
	 *            The port to host the server on
	 * @throws IOException
	 */
	public Server(int port) throws IOException {
		clients = new ArrayList<>();
		activeGames = new ArrayList<>();
		ssocket = AsynchronousServerSocketChannel.open();
		ssocket.bind(new InetSocketAddress(port));
		startAcceptClient();
		System.out.println("Server started and listening on port " + port);
	}

	public void startAcceptClient() {
		ssocket.accept(null, new Util.SimpleHandler<>(con -> connectSocket(con), ex -> ex.printStackTrace()));
	}

	/**
	 * Deals with new clients and adds them to the game
	 * 
	 * @param sock
	 *            The socket that is connected to the new client
	 */
	private void connectSocket(AsynchronousSocketChannel sock) {
		ClientConnection client = new ClientConnection(this, sock);
		clients.add(client);
		System.out.println("client connected, total: " + clients.size());
		startAcceptClient();
	}

	/**
	 * removes a client from the server and closes the connection is the client
	 * is still connected
	 * 
	 * @param client
	 *            client to remove
	 */
	public void disconnectClient(ClientConnection client) {
		client.disconnect();
		clients.remove(client);
		System.out.println("Client disconnected, clients left: " + clients.size());
		broadcastPlayers();
	}

	/**
	 * Checks all clients and starts a game if enough players are queued
	 */
	public void findQueue() {
		ArrayList<ClientConnection> cons = new ArrayList<>();
		for (ClientConnection con : clients) {
			if (con.getState() == SessionState.queued) {
				cons.add(con);
			}
		}
		System.out.println("total queued: " + cons.size());
		for (int offset = 0; offset + Game.NUMBER_PLAYERS - 1 < cons.size(); offset += Game.NUMBER_PLAYERS) {
			ArrayList<Player> players = new ArrayList<>();
			for (int i = 0; i < Game.NUMBER_PLAYERS; i++) {
				players.add(new RemotePlayer(cons.get(offset + i), i));
			}
			startGame(players);
		}
		broadcastPlayers();
	}

	/**
	 * Start a game with the players in players
	 * 
	 * @param players
	 *            players to add to the game
	 */
	public void startGame(ArrayList<Player> players) {
		System.out.println("Starting game with " + players.get(0) + ", " + players.get(1));
		Game game = new Game(players);
		for (Player p : players) {
			p.setGame(game);
		}
		game.startGame();
		activeGames.add(game);
		broadcastPlayers();
		game.addObserver(this);
	}

	public void closeGame(Game game) {
		System.out.println("Closing game");
		activeGames.remove(game);
		game.deleteObserver(this);
		broadcastPlayers();
	}

	/**
	 * Find the player using playername
	 * 
	 * @param playername
	 *            playername to look for
	 * @return player object if a player with player name is found, null
	 *         otherwise
	 */
	public ClientConnection findPlayer(String playername) {
		for (ClientConnection p : clients) {
			if (p.getName().equals(playername)) {
				return p;
			}
		}
		return null;
	}

	public void broadcastPlayers() {
		String players = "";
		for (ClientConnection con : clients) {
			if (!con.isLoggedIn()) {
				continue;
			}
			if (!players.isEmpty()) {
				players += " ";
			}
			players += con.getName() + "-" + con.getState();
		}
		for (ClientConnection con : clients) {
			if (!con.isLoggedIn()) {
				continue;
			}
			con.sendString(Protocol.PLAYERS + Protocol.DELIMITER + players);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Game) {
			Game game = (Game) o;
			if (arg == EventType.placed) {
				if (game.getState() != GameState.onGoing) {
					closeGame(game);
				}
			}
		}
	}
}























