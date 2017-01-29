package client.command;

import client.Session;
import common.SessionState;


public class CancelQueueHandler extends CommandHandler {

	public CancelQueueHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public boolean handle(String[] parts) {
		getSession().cancelQueueGame();
		System.out.println("happening");
		return true;
	}
}
