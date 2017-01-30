package client.command;

import java.util.ArrayList;

import client.Session;
import common.SessionState;

public class InviteHandler extends CommandHandler {
	public InviteHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public boolean handle(String[] parts) {
		int index = 1;
		int size;
		try {
			size = Integer.parseInt(parts[index++]);
		} catch (NumberFormatException ex) {
			this.setErrorMessage("invalid board size format");
			return false;
		}
		
		ArrayList<String> players = new ArrayList<>();
		while (index < parts.length) {
			players.add(parts[index++]);
		}
		
		this.getSession().invite(size, players);
		return true;
	}
	
}
