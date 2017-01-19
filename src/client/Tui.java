package client;

import java.io.PrintStream;
import java.util.Observable;
import java.util.Scanner;

import common.Protocol;
import common.SessionState;
import common.Ui;

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
		case "login":
			if (parts.length < 3) {
				out.println("Usage: login <name> <host> [port]");
				break;
			}
			String host = parts[2];
			int port = Protocol.DEFAULTPORT;
			if (parts.length >= 4) {
				try {
					port = Integer.parseUnsignedInt(parts[3]);
				} catch (NumberFormatException e) {
					out.println("Invalid port format");
					break;
				}
			}

			session.connect(host, port);
			break;

		case "queue":
			if (session.getState() != SessionState.lobby) {
				out.println("You need to be in the lobby to queue");
				break;
			}
			session.queueGame();
			break;

		case "test":
			session.connect("localhost", Protocol.DEFAULTPORT);
			break;

		default:
			out.println("command not recognised, the commands are:");
			out.println("login <name> <host> [port]");
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
		case game:
			break;
		case gamemove:
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
		}
	}
}






















