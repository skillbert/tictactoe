package ai;

import java.awt.Point;

import common.Board;
import common.Game;
import common.MultiThreadTask;

/**
 * A threaded extension of bruteforceAI that starts the lowest branch in
 * different threads.
 * 
 * @author Wilbert
 *
 */
public class ThreadedBruteforceAI extends BruteForceAI {
	private static final double goalPerThread = 40e6;
	private int[] beginFields;
	private int[] beginMoves;
	private int[] resultScores;
	
	public ThreadedBruteforceAI(Game game, int mark) {
		super(game, mark);
	}
	
	@Override
	public Point thinkMove(Board board) {
		beginFields = game.getBoard().getFieldsClone();
		beginMoves = AIUtil.boardColumnIndexes(game.getBoard());
		resultScores = new int[layerSize];
		int nThreads = Runtime.getRuntime().availableProcessors();
		setIterationGoal(beginMoves, nThreads * goalPerThread);
		
		MultiThreadTask task = new MultiThreadTask();
		for (int index = 0; index < layerSize; index++) {
			task.AddTask(i -> asyncRecursiveMove((int) i), index);
		}
		
		task.addThreads(nThreads);
		try {
			task.joinAll();
		} catch (InterruptedException ex) {
			// TODO do something meaningful here
			ex.printStackTrace();
			return new Point(0, 0);
		}
		return asyncDone();
	}
	
	/**
	 * To be run when all branches are complete
	 */
	private Point asyncDone() {
		int best = 0;
		int bestindex = -1;
		for (int index = 0; index < layerSize; index++) {
			if (beginMoves[index] == Board.INVALID_INDEX || beginMoves[index] >= maxFieldIndex) {
				continue;
			}
			if (bestindex == -1 || resultScores[index] > best) {
				best = resultScores[index];
				bestindex = index;
			}
		}
		return game.getBoard().position(bestindex);
	}
	
	/**
	 * A single branch that is run by a separate thread
	 * 
	 * @param index
	 *            the starting move of this branch
	 */
	private void asyncRecursiveMove(int index) {
		int[] moves = beginMoves.clone();
		int[] fields = beginFields.clone();
		
		int move = moves[index];
		if (move >= maxFieldIndex || move == Board.INVALID_INDEX) {
			return;
		}
		
		fields[move] = myMark;
		moves[index] += layerSize;
		int newmark = (myMark + 1) % nPlayers;
		int change = recursiveMove(fields, moves, newmark, move, maxdepth - 1).score;
		
		if (debugprint) {
			printMove(move, change, maxdepth, myMark);
		}
		
		resultScores[index % layerSize] = change;
	}
}






