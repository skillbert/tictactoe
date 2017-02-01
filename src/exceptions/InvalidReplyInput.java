package exceptions;

public class InvalidReplyInput extends ValidationError {
	public InvalidReplyInput() {
		
	}
	
	@Override
	public String getMessage() {
		return("Please reply \"yes\" or \"no\"");
	}
}
