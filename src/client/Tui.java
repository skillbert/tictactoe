package client;

import java.io.PrintStream;
import java.util.Observable;
import java.util.Scanner;

import common.Game;
import common.Protocol;
import common.SessionState;

public class Tui implements Ui {
	private PrintStream out;
	private Scanner in;
	private Session session;

	public Tui(Session session) {
		out = System.out;
		in = new Scanner(System.in);
		this.session = session;
	}

	@Override
	public void run() {
		out.println("hello to tictactoe");
		while (true) {
			parseInput(in.nextLine());
		}
	}

	private void parseInput(String input) {
		String[] parts = input.split(" ");

		switch (parts[0]) {
		case "connect":
			if (parts.length < 2) {
				out.println("Usage: login <host> [port]");
				break;
			}
			if (session.getState() != SessionState.disconnected) {
				out.println("already connected");
				break;
			}
			String host = parts[1];
			int port = Protocol.DEFAULTPORT;
			if (parts.length >= 3) {
				try {
					port = Integer.parseUnsignedInt(parts[2]);
				} catch (NumberFormatException e) {
					out.println("Invalid port format");
					break;
				}
			}

			session.connect(host, port);
			break;

		case "login":
			if (parts.length < 2) {
				out.println("Usage: login <name>");
				break;
			}
			if (session.getState() != SessionState.authenticating) {
				if (session.getState() == SessionState.disconnected | session.getState() == SessionState.connecting) {
					out.println("You need to connect first before you can log in");
				} else {
					out.println("Already logged in");
				}
				break;
			}
			session.login(parts[1]);
			break;

		case "queue":
			if (session.getState() != SessionState.lobby) {
				if (session.getState() == SessionState.queued) {
					out.println("You are already queued");
				} else {
					out.println("You need to be in the lobby to queue");
				}
				break;
			}
			session.queueGame();
			break;

		case "test":
			session.connect("localhost", Protocol.DEFAULTPORT);
			break;

		case "place":
			if (parts.length < 3) {
				out.println("Usage: place <x> <y>");
				break;
			}
			if (session.getState() != SessionState.ingame) {
				out.println("Not in a game");
				break;
			}
			int x, y;
			try {
				x = Integer.parseUnsignedInt(parts[1]);
				y = Integer.parseUnsignedInt(parts[2]);
			} catch (NumberFormatException ex) {
				out.println("invalid number format");
				break;
			}
			session.commitMove(x, y);
			break;

		default:
			out.println("command not recognised, the commands are:");
			out.println("login <host> [port]");
			out.println("login <name>");
			out.println("queue");
			break;
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
			out.println("Connected to the server, you are now in the lobby");
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
}






















