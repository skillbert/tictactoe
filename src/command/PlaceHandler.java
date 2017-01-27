package command;

import client.Session;
import common.SessionState;

public class PlaceHandler extends CommandHandler {
	public PlaceHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage){
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public boolean handle(String[] parts) {
		int x, y;
		try {
			x = Integer.parseUnsignedInt(parts[1]);
			y = Integer.parseUnsignedInt(parts[2]);
		} catch (NumberFormatException ex) {
			this.setErrorMessage("invalid number format");
			return false;
		}
		if (x <= 4 && y <= 4) { // 4 because user input 1-4
			getSession().commitMove(x - 1, y - 1); // -1 because 0 indexed vs. 1
												// indexed as presented to the
												// user.
		} else {
			this.setErrorMessage("Invalid input, make sure 1 <= x <= 4 and 1 <= y <= 4");
			return false;
		}
		getSession().commitMove(x, y);
		return true;
	}
}
