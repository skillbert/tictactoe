package client.command;

import client.Session;
import common.Protocol;
import common.SessionState;

public class TestHandler extends CommandHandler {
	
	public TestHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage){
		super(session, minArgs, usage, requiredState, wrongStateMessage);
	}
	
	@Override
	public void handle(String[] parts) {
		getSession().connect("localhost", Protocol.DEFAULTPORT);
	}
	
}
