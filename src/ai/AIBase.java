package ai;

import java.awt.Point;
import java.util.ArrayList;

import common.AsyncSocket.Callback1;
import common.Board;
import common.Game;

public abstract class AIBase {
	protected Game game;
	protected ArrayList<int[]> wincons;
	protected int myMark;
	protected Callback1<Point> onDone;
	
	/**
	 * Initializes an AIPlayer with name and mark
	 * 
	 * @param name
	 *            AIPlayer name to use
	 * @param mark
	 *            AIPlayer mark to use
	 */
	public AIBase(Game game, int mark) {
		this.myMark = mark;
		this.game = game;
		this.wincons = (ArrayList<int[]>) game.getBoard().getWinConditions().clone();
	}
	
	public void startThinkMove(Callback1<Point> donefunc) {
		this.onDone = donefunc;
		Thread thread = new Thread(() -> thinkThread());
		thread.start();
	}
	
	private void thinkThread() {
		Point move = thinkMove(game.getBoard());
		onDone.run(move);
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
	protected abstract Point thinkMove(Board board);
}
