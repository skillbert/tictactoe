package client.command;

import client.Session;
import common.SessionState;

public abstract class CommandHandler {
	private final Session session;
	private String errorMessage;
    public final int minArgs;
    public final String usage;
    public final SessionState requiredState;
    public final String wrongStateMessage;
    
	public CommandHandler(Session session, int minArgs, String usage, SessionState requiredState, String wrongStateMessage) {
		this.session = session;
        this.minArgs = minArgs;
        this.usage = usage;
        this.requiredState = requiredState;
        this.wrongStateMessage = wrongStateMessage;
	}
	
    public Session getSession() {
    		return this.session;
    }
    
    public String getErrorMessage() {
    		return this.errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
    		this.errorMessage = errorMessage;
    }
    
    public boolean validateArgs(String[] parts) {
    		System.out.println("validate args");
    		if (parts.length < minArgs) {
        		System.out.println("invalid args");

    			setErrorMessage(String.format("Usage: %s %s", parts[0], usage));
    			return false;
    		}
    		System.out.println("valid args");

    		return true;
    }
    
    public boolean validateState() {
		System.out.println("validate state");

    		if (session.getState() != requiredState) {
    			System.out.println("invalid state" + session.getState() + requiredState);

    			setErrorMessage(wrongStateMessage);
    			return false;
    		}
    		System.out.println("valid state" + session.getState() + requiredState);

    		return true;
    }
    
	public abstract boolean handle(String[] parts);
}
