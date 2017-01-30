package common;

public class Mark {
	public static final int EMPTY = -1;
	public static final int RED = 0;
	public static final int YELLOW = 1;
	public static final int BLUE = 2;
	
	
	public static String getString(int mark) {
		switch (mark) {
			case EMPTY:
				return "empty";
			case RED:
				return "red";
			case YELLOW:
				return "yellow";
			case BLUE:
				return "blue";
			default:
				return "unknown";
		}
	}
	
	public static String getMarkString(int mark) {
		switch (mark) {
			case EMPTY:
				return " ";
			case RED:
				return "X";
			case YELLOW:
				return "O";
			case BLUE:
				return "#";
			default:
				return "?";
		}
	}
}
