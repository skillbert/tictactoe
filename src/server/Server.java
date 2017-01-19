package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;

import common.Game;
import common.Mark;
import common.Player;
import common.Protocol;
import common.RemotePlayer;
import common.SessionState;
import common.Util;

public class Server {
	private static int playerindex = 0;

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
		ssocket.bind(new InetSocketAddress("localhost", port));
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
		ClientConnection client = new ClientConnection(this, sock, "Player " + (++playerindex));
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

	public void findQueue() {
		ClientConnection[] cons = (ClientConnection[]) clients.stream().filter(c -> c.getState() == SessionState.queued)
				.limit(Game.NUMBER_PLAYERS).toArray();
		if (cons.length == Game.NUMBER_PLAYERS) {
			RemotePlayer[] players = new RemotePlayer[cons.length];
			for (int i = 0; i < cons.length; i++) {
				players[i] = new RemotePlayer(cons[i], Mark.fromInt(i));
			}
		}
	}

	public void startGame(Player[] players) {
		Game game = new Game(players);

		// tell all our remote players about this game
		for (Player player : players) {
			if (player instanceof RemotePlayer) {
				((RemotePlayer) player).getConnection().setState(SessionState.ingame);
			}
		}

		game.startGame();
	}
}























