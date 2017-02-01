package client.command;

import client.Session;
import common.SessionState;

public class LoginHandler extends CommandHandler {
	public LoginHandler(Session session, int minArgs, String usage, 
			SessionState requiredState, String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) {
		getSession().login(parts[1]);
	}
	
}
