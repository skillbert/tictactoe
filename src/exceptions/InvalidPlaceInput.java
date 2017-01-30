package exceptions;

public class InvalidPlaceInput extends ValidationError {
	private int y;
	private int x;

	public InvalidPlaceInput(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String getMessage() {
		return String.format("The selected field at (%d, %d) is not in range", this.x, this.y);
	}
}
