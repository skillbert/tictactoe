package client.command;

import client.Session;
import common.SessionState;


public class CancelQueueHandler extends CommandHandler {

	public CancelQueueHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) {
		getSession().cancelQueueGame();
	}
}
