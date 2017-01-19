package common;

public abstract interface Player {
	public String getName();

	public Mark getMark();

	public abstract int determineMove(Board board);

	public void makeMove(Board board);
}
