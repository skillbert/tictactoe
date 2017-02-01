package exceptions;

public class InvalidBoardSize extends ValidationError {
	public InvalidBoardSize() {
		
	}
	
	@Override
	public String getMessage() {
		return("invalid board size format");
	}
}
