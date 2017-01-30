package client.command;

import client.Session;
import common.SessionState;

public class PlaceHandler extends CommandHandler {
	public PlaceHandler(Session session, int minArgs, String usage, SessionState requiredState,
			String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public boolean handle(String[] parts) {
		int x, y;
		try {
			// subtract 1 to go from 1 based user view to 0 based internal model
			x = Integer.parseUnsignedInt(parts[1]) - 1;
			y = Integer.parseUnsignedInt(parts[2]) - 1;
		} catch (NumberFormatException ex) {
			this.setErrorMessage("invalid number format");
			return false;
		}
		int boardSize = getSession().getGame().getBoard().getSize();
		if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
			this.setErrorMessage("Invalid input, make sure 1 <= x <= " + boardSize
					+ " and 1 <= y <= " + boardSize);
			return false;
		}
		getSession().commitMove(x, y);
		return true;
	}
}
