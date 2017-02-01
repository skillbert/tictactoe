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

public class SuggestMoveHandler extends CommandHandler {
	public SuggestMoveHandler(Session session, int minArgs, String usage,
			SessionState requiredState, String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) throws UnknownAIException, NotYourTurnException {
		if (!AIBase.getAiTypes().containsKey(parts[1])) {
			throw new UnknownAIException();
		}
		AIType aiType = AIBase.getAiTypes().get(parts[1]);
		
		String myname = getSession().getMyName();
		ArrayList<? extends Player> players = getSession().getGame().getPlayers();
		Optional<? extends Player> optplayer = players.stream()
				.filter(p -> p.getName().equals(myname)).findAny();
		
		if (!optplayer.isPresent()) {
			throw new NotYourTurnException();
		}
		
		Player player = optplayer.get();
		if (getSession().getGame().getTurn() != player) {
			throw new NotYourTurnException();
		}
		
		AIBase.startGetSingleMove(getSession().getGame(), player.getMark(), aiType,
				point -> getSession().getUI().showMoveSuggestion(point));
		
	}
	
}
