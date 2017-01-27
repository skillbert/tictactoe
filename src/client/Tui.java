package client;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;

import command.*;
import common.Game;
import common.SessionState;

public class Tui implements Ui {
	private PrintStream out;
	private Scanner in;
	private Session session;
	private Map<String, CommandHandler> commands = new HashMap<String, CommandHandler>();
	
	public Tui(Session session) {
		out = System.out;
		in = new Scanner(System.in);
		this.session = session;
		
		commands.put("connect", new ConnectHandler(session, 2, "<host> [port]", SessionState.disconnected,
				"Already connected"));
		commands.put("login", new LoginHandler(session, 2, "<name>", SessionState.authenticating,
				"Not connected or already logged in"));
		commands.put("queue", new QueueHandler(session, 1, "", SessionState.lobby,
				"Already queued or not in the lobby"));
		commands.put("test", 
				new TestHandler(session, 1, "", SessionState.disconnected, "Already connected"));
		commands.put("place",
				new PlaceHandler(session, 3, "<x> <y>", SessionState.ingame, "Not in a game"));
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
	
	private void parseInput(String input) {
		String[] parts = input.split(" ");
		if (commands.containsKey(parts[0])) {
			CommandHandler handler = commands.get(parts[0]);
			if (handler.validateArgs(parts) && handler.validateState()) {
				if (handler.handle(parts)){
					return;
				}
			}
			out.println(handler.getErrorMessage());
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
				out.println(
						"Queued for random game. A game will start when another player enters the queue.");
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
		for (CommandHandler command:this.commands.values()){
			out.println(command.usage);
		}
	}
}






















