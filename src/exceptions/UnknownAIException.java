package exceptions;

public class UnknownAIException extends ValidationError {
	@Override
	public String getMessage() {
		return "Valid bot names are: easy, medium, hard";
	}
}
