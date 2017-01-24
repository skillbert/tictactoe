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

	public ClientConnection(Server server, AsynchronousSocketChannel asyncChannel) {
		this.server = server;
		this.state = SessionState.authenticating;
		this.name = "";

		setSocket(new AsyncSocket(asyncChannel));
	}

	private void setSocket(AsyncSocket sock) {
		this.sock = sock;
		sock.onClose(() -> server.disconnectClient(this));
		sock.onMessage(str -> parseMessage(str));
	}

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

	private void commitMove(int x, int y) {
		if (state != SessionState.ingame) {
			sendString("error invalidMove");
			System.out.println("invalid move 3 " + state);
			return;
		}
		player.getGame().commitMove(player, y, x);
	}

	public void queueGame() {
		if (state != SessionState.lobby) {
			showModalMessage("You need to be in the lobby to queue");
			return;
		}
		state = SessionState.queued;
		sendString("waiting");
		server.findQueue();
	}

	public void unQueueGame() {
		if (state != SessionState.queued) {
			showModalMessage("You need to be queued in order to leave the queue");
			return;
		}
		state = SessionState.lobby;
		// TODO non-standard protocol
		sendString("lobby");
	}

	private void setName(String name) {
		if (state != SessionState.authenticating) {
			showModalMessage("Already logged in. You can't set your name at this time");
			return;
		}
		// TODO the protocol doesn't actually specify a legal character, "word"
		// characters is assumed here
		// \W matches anything besides a-zA-Z0-9_
		// java regex apparently doesn't have a simple way to match for
		// substrings, so .* .*
		if (name.matches(".*\\W.*")) {
			sendString("error invalidCharacters");
			return;
		}
		if (server.findPlayer(name) != null) {
			sendString("error nameTaken");
			return;
		}

		this.name = name;
		state = SessionState.lobby;

		// TODO the standard requires us to instantly join the queue
		if (Protocol.followStandards) {
			queueGame();
		}
	}

	public void disconnect() {
		sock.close();
	}

	public String getName() {
		return name;
	}

	public SessionState getState() {
		return state;
	}

	public void setState(SessionState state) {
		this.state = state;
	}

	public void showModalMessage(String message) {
		sendString("error errorMessage " + message);
	}

	public void sendString(String str) {
		System.out.println(name + "\t<< " + str);
		sock.sendString(str);
	}

	public void setPlayer(RemotePlayer player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}


























