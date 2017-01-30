package client.command;

import client.Session;
import common.Protocol;
import common.SessionState;
import exceptions.InvalidPlaceInput;
import exceptions.ValidationError;

public class PlaceHandler extends CommandHandler {
	public PlaceHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage){
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) throws ValidationError, NumberFormatException {
		int x, y;
		x = Integer.parseUnsignedInt(parts[1]);
		y = Integer.parseUnsignedInt(parts[2]);

		if (x > 0 && x <= 4 && y > 0 && y <= 4) { // 4 because user input 1-4
			getSession().commitMove(x - 1, y - 1); // -1 because 0 indexed vs. 1
												// indexed as presented to the
												// user.
		} else {
			throw new InvalidPlaceInput(x, y);
		}
	}
}
