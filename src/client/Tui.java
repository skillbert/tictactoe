package client;

import java.awt.Point;
import java.io.PrintStream;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;

import client.command.CommandHandler;
import client.command.Commands;
import common.Game;
import common.SessionState;
import exceptions.ValidationError;

public class Tui implements Ui {
	private PrintStream out;
	private Scanner in;
	private Session session;
	public final Map<String, CommandHandler> commands;
	
	public Tui(Session session) {
		out = System.out;
		in = new Scanner(System.in);
		this.session = session;
		Commands commandsClass = new Commands(this.session);
		commands = commandsClass.commands;
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
			try {
				handler.validateArgs(parts);
				handler.validateState();
				handler.handle(parts);
			} catch (NumberFormatException | ValidationError e) {
				showModalMessage(e.getMessage());
				
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
			case lobby:
				playersChanged();
				break;
		}
	}
	
	@Override
	public void showModalMessage(String message) {
		out.println(message);
	}
	
	private void stateChanged() {
		// TODO show invitations on statechange to invited
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
					"Queued for random game. "
					+ "A game will start when another player enters the queue.");
				break;
			case invited:
				GameInvitation invite = session.getInvite();
				if (invite != null) {
					out.println(String.format("You were invited for a game by %s",
							invite.getInviter()));
				}
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
	
	private void playersChanged() {
		if (session.getState() == SessionState.lobby || session.getState() == SessionState.queued) {
			out.println("Connected players:");
			Map<String, String> playerLobbyData = session.getPlayerLobbyData();
			for (Map.Entry<String, String> entry : playerLobbyData.entrySet()) {
				out.println(
						String.format("name: %s | status: %s", entry.getKey(), entry.getValue()));
			}
		}
	}
	
	private void printHelp() {
		for (String commandName : this.commands.keySet()) {
			CommandHandler command = this.commands.get(commandName);
			out.println(String.format("%s %s", commandName, command.usage));
		}
	}
	
	@Override
	public void showMoveSuggestion(Point point) {
		out.println((point.x + 1) + " " + (point.y + 1) + " looks like a good move");
	}
}






















