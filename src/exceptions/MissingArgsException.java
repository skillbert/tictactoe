package exceptions;

public class MissingArgsException extends ValidationError {
	private int argCount;
	private int minArgs;

	public MissingArgsException(int argCount, int minArgs) {
		this.argCount = argCount;
		this.minArgs = minArgs;
	}
	
	@Override
	public String getMessage() {
		return String.format("Total of %d args suppplied, at least %d needed", this.argCount, this.minArgs);
	}
}
