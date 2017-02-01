package client.command;

import ai.AIBase;
import client.Session;
import common.SessionState;
import exceptions.UnknownAIException;


public class BotGameHandler extends CommandHandler {
	
	public BotGameHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) throws UnknownAIException {
		if (!AIBase.getAiTypes().containsKey(parts[1])) {
			throw new UnknownAIException();
		}
		getSession().startBotGame(parts[1]);
	}
}
