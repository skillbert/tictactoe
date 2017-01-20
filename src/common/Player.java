package common;

import java.util.Observer;

public abstract interface Player extends Observer {
	public String getName();

	public Mark getMark();

	public abstract int determineMove(Board board);

	public void makeMove(Board board);

	public void showModalMessage(String message);
}
