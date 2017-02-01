package client.command;

import java.util.ArrayList;
import java.util.Optional;

import ai.AIBase;
import ai.AIType;
import client.Session;
import common.Player;
import common.SessionState;
import exceptions.NotYourTurnException;
import exceptions.UnknownAIException;

public class ToggleBotHandler extends CommandHandler {
	public ToggleBotHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) throws UnknownAIException, NotYourTurnException {
		if (parts[1].equals("off")) {
			getSession().setAiMoveSource(null);
		} else if (AIBase.getAiTypes().containsKey(parts[1])) {
			AIType aiType = AIBase.getAiTypes().get(parts[1]);
			
			String myname = getSession().getMyName();
			ArrayList<? extends Player> players = getSession().getGame().getPlayers();
			Optional<? extends Player> optplayer = players.stream()
					.filter(p -> p.getName().equals(myname)).findAny();
			
			if (!optplayer.isPresent()) {
				throw new NotYourTurnException();
			}
			
			Player player = optplayer.get();
			
			getSession().setAiMoveSource(
					AIBase.getAi(getSession().getGame(), player.getMark(), aiType));
		} else {
			throw new UnknownAIException();
		}
	}
}
