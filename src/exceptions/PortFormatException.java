package exceptions;

public class PortFormatException extends NumberFormatException {
	public PortFormatException() {
		super("Invalid port format");
	}
}
