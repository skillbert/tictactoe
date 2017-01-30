package client.command;

import client.Session;
import common.SessionState;
import exceptions.InvalidPlaceInput;
import exceptions.ValidationError;

public class PlaceHandler extends CommandHandler {
	public PlaceHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) throws ValidationError, NumberFormatException {
		int x, y;
		// subtract 1 to go from 1 based user view to 0 based internal model
		x = Integer.parseUnsignedInt(parts[1]) - 1;
		y = Integer.parseUnsignedInt(parts[2]) - 1;
		
		int boardSize = getSession().getGame().getBoard().getSize();
		if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
			throw new InvalidPlaceInput(x, y);
		}
		getSession().commitMove(x, y);
		
	}
}

