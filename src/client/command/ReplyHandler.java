package client.command;

import client.Session;
import common.SessionState;

public class ReplyHandler extends CommandHandler {
	public ReplyHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public boolean handle(String[] parts) {
		if (!parts[1].equals("yes") && !parts[1].equals("no")) {
			setErrorMessage("Please reply \"yes\" or \"no\"");
			return false;
		}
		
		getSession().replyInvite(parts[1].equals("yes"));
		return true;
	}
	
}
