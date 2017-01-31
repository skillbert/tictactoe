package client.command;

import client.Session;
import common.SessionState;
import exceptions.InvalidReplyInput;

public class ReplyHandler extends CommandHandler {
	public ReplyHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) throws InvalidReplyInput {
		if (!parts[1].equals("yes") && !parts[1].equals("no")) {
			throw new InvalidReplyInput();
		}
		getSession().replyInvite(parts[1].equals("yes"));
	}
	
}
