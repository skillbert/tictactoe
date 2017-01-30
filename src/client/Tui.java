package client;

import java.io.PrintStream;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;

import client.command.*;
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
		Commands Commands = new Commands(this.session);
		commands = Commands.commands;
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
		for (String commandName:this.commands.keySet()){
			CommandHandler command = this.commands.get(commandName);
			out.println(String.format("%s %s", commandName, command.usage));
		}
	}
}






















