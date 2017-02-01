package exceptions;

import common.SessionState;

public class WrongStateException extends ValidationError {
	private SessionState currentState;
	private SessionState requiredState;

	public WrongStateException(SessionState currentState, SessionState requiredState) {
		this.currentState = currentState;
		this.requiredState = requiredState;
	}
	
	@Override
	public String getMessage() {
		return String.format("Required state is %s while the current state is %s", 
				this.requiredState, this.currentState);
	}
}
