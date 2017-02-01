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
		// returns x/y + 1 to go from internal model to UI model.
		return String.format("The selected field at (%d, %d) is not in range", this.x + 1, this.y + 1);
	}
}
