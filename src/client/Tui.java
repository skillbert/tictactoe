package client;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;

import common.Game;
import common.Protocol;
import common.SessionState;

public class Tui implements Ui {
	private PrintStream out;
	private Scanner in;
	private Session session;
	private Map<String, CommandData> commands = new HashMap<String, CommandData>();

	public Tui(Session session) {
		out = System.out;
		in = new Scanner(System.in);
		this.session = session;

		commands.put("connect",
				new CommandData(2, "<host> [port]", SessionState.disconnected, "Already connected", "handleConnect"));
		commands.put("login", new CommandData(2, "<name>", SessionState.authenticating,
				"Not connected or already logged in", "handleLogin"));
		commands.put("queue",
				new CommandData(1, "", SessionState.lobby, "Already queued / Not in the lobby", "handleQueue"));
		commands.put("test", new CommandData(1, "", SessionState.disconnected, "Already connected", "handleTest"));
		commands.put("place", new CommandData(3, "<x> <y>", SessionState.ingame, "Not in a game", "handlePlace"));
	}

	@Override
	public void run() {
		out.println("Connect 3D");
		out.println("====================");
		out.println("\nWelcome, the available commands are:\n");
		printHelp();
		while (true) {
			parseInput(in.nextLine());
		}
	}

	public void handleConnect(String[] parts) {
		String host = parts[1];
		int port = Protocol.DEFAULTPORT;
		if (parts.length >= 3) {
			try {
				port = Integer.parseUnsignedInt(parts[2]);
			} catch (NumberFormatException e) {
				out.println("Invalid port format");
			}
		}

		session.connect(host, port);
	}

	public void handleLogin(String[] parts) {
		session.login(parts[1]);
	}

	public void handleQueue(String[] parts) {
		session.queueGame();
	}

	public void handlePlace(String[] parts) {
		int x, y;
		try {
			x = Integer.parseUnsignedInt(parts[1]);
			y = Integer.parseUnsignedInt(parts[2]);
		} catch (NumberFormatException ex) {
			out.println("invalid number format");
			return;
		}
		if (x <= 4 && y <= 4) { // 4 because user input 1-4
			session.commitMove(x - 1, y - 1); // -1 because 0 indexed vs. 1
												// indexed as presented to the
												// user.
		} else {
			out.println("Invalid input, make sure 1 <= x <= 4 and 1 <= y <= 4");
		}
	}

	public void handleTest(String[] parts) {
		session.connect("localhost", Protocol.DEFAULTPORT);
	}

	private void parseInput(String input) {
		String[] parts = input.split(" ");
		if (commands.containsKey(parts[0])) {
			CommandData commandData = commands.get(parts[0]);
			if (parts.length < commandData.minArgs) {
				out.println(String.format("Usage: %s %s", parts[0], commandData.usage));
				return;
			}
			if (session.getState() != commandData.requiredState) {
				out.println(commandData.wrongStateMessage);
				return;
			}

			try {
				this.getClass().getMethod(commandData.handler, new Class[] { String[].class }).invoke(this,
						new Object[] { parts });
			} catch (Exception e) {
				out.println(e);
			}

		} else {
			out.println("Command not recognised, the commands are:");
			printHelp();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		switch ((Ui.UpdateType) arg) {
		case state:
			stateChanged();
			break;
		case gamemove:
			gameChanged();
			break;
		}
	}

	@Override
	public void showModalMessage(String message) {
		out.println(message);
	}

	private void stateChanged() {
		SessionState newstate = session.getState();
		switch (newstate) {
		case connecting:
			out.println("Connecting to server...");
			break;
		case authenticating:
			out.println("Connected, please choose a name");
			break;
		case lobby:
			out.println("You are now in the lobby");
			break;
		case queued:
			out.println("Queued for random game. A game will start when another player enters the queue.");
			break;
		case disconnected:
			out.println("Disconnected from server");
			break;
		case ingame:
			out.println("entered game");
			gameChanged();
			break;
		}
	}

	private void gameChanged() {
		Game game = session.getGame();
		out.println(game);
	}

	private void printHelp() {
		out.println("connect <host> [port]");
		out.println("login <name>");
		out.println("queue");
		out.println("place <x> <y>");
		out.println("test");
	}
}






















