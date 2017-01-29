package client.command;

import client.Session;
import common.Protocol;
import common.SessionState;

public class ConnectHandler extends CommandHandler {
	
	public ConnectHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage) {
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public boolean handle(String[] parts) {
		String host = parts[1];
		int port = Protocol.DEFAULTPORT;
		if (parts.length >= 3) {
			try {
				port = Integer.parseUnsignedInt(parts[2]);
			} catch (NumberFormatException e) {
				this.setErrorMessage("Invalid port format");
				return false;
			}
		}
		getSession().connect(host, port);
		return true;
	}
	
}
