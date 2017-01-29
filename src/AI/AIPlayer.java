package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import common.Board;
import common.Game;
import common.Player;

public abstract class AIPlayer implements Player {
	protected Game game;
	protected ArrayList<int[]> wincons;
	protected int myMark;
	private String name;
	
	/**
	 * Initializes an AIPlayer with name and mark
	 * 
	 * @param name
	 *            AIPlayer name to use
	 * @param mark
	 *            AIPlayer mark to use
	 */
	public AIPlayer(String name, int mark) {
		this.myMark = mark;
		this.name = name;
	}
	
	@Override
	public void setGame(Game game) {
		this.game = game;
		game.addObserver(this);
		wincons = (ArrayList<int[]>) game.getBoard().getWinConditions().clone();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getMark() {
		return myMark;
	}
	
	@Override
	public void showModalMessage(String message) {
	}
	
	@Override
	public void obtainTurn() {
		Point move = thinkMove(game.getBoard());
		game.commitMove(this, move.y, move.x);
		int a = 0;
	}
	
	@Override
	public void update(Observable o, Object arg) {
	}
	
	/**
	 * Performs the calculations to determine the move to make and returns the
	 * move.
	 * 
	 * @param board
	 *            Board object to use
	 * @return the move to make as a Point object or null if the thinking will
	 *         be done async
	 */
	public abstract Point thinkMove(Board board);
}
