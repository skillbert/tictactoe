package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;

import common.Game;
import common.Player;
import common.Protocol;
import common.RemotePlayer;
import common.SessionState;
import common.Util;

public class Server {
	private AsynchronousServerSocketChannel ssocket;
	private ArrayList<ClientConnection> clients;

	public static void main(String[] args) throws IOException {
		try {
			new Server(Protocol.DEFAULTPORT);
		} catch (IOException ex) {
			System.out.println("Server error " + ex.getMessage());
		}
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
	 */
	public void disconnectClient(ClientConnection client) {
		client.disconnect();
		clients.remove(client);
		System.out.println("Client disconnected, clients left: " + clients.size());
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
	}

	public void startGame(ArrayList<Player> players) {
		System.out.println("Starting game with " + players.get(0) + ", " + players.get(1));
		Game game = new Game(players);
		for (Player p : players) {
			p.setGame(game);
		}
		game.startGame();
	}

	public ClientConnection findPlayer(String playername) {
		for (ClientConnection p : clients) {
			if (p.getName().equals(playername)) {
				return p;
			}
		}
		return null;
	}
}























