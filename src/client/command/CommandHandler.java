package client.command;

import javax.xml.bind.ValidationException;

import client.Session;
import common.SessionState;
import exceptions.MissingArgsException;
import exceptions.ValidationError;
import exceptions.WrongStateException;

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
    
    public void validateArgs(String[] parts) throws MissingArgsException {
    		if (parts.length < minArgs) {
    			throw new MissingArgsException(minArgs, parts.length);
    		}
    }
    
    public void validateState() throws WrongStateException {

    		if (session.getState() != requiredState) {
    			throw new WrongStateException(session.getState(), requiredState);
    		}
    }
    
	public abstract void handle(String[] parts) throws ValidationError, NumberFormatException;
}
