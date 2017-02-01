package exceptions;

public class NotYourTurnException extends ValidationError {
	@Override
	public String getMessage() {
		return "It is not your turn";
	}
}
