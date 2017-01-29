package ai;

import java.awt.Point;

import common.Board;
import common.MultiThreadTask;

/**
 * A threaded extension of bruteforceAI that starts the lowest branch in
 * different threads.
 * 
 * @author Wilbert
 *
 */
public class ThreadedBruteforceAI extends BruteForceAI {
	private int nThreads;
	private int[] beginFields;
	private int[] beginMoves;
	private int[] resultScores;
	
	public ThreadedBruteforceAI(String name, int mark) {
		super(name, mark);
		maxdepth = 7;
		nThreads = 8;
	}
	
	@Override
	public synchronized void obtainTurn() {
		Thread thread = new Thread(() -> thinkThread());
		thread.start();
	}
	
	/**
	 * The code by the main thread that starts the other threads
	 */
	private void thinkThread() {
		beginFields = game.getBoard().getFieldsClone();
		beginMoves = AIUtil.boardColumnIndexes(game.getBoard());
		resultScores = new int[layerSize];
		
		MultiThreadTask task = new MultiThreadTask();
		for (int index = 0; index < layerSize; index++) {
			task.AddTask(i -> asyncRecursiveMove((int) i), index);
		}
		
		task.onFinish(arg -> asyncDone(), null);
		task.addThreads(nThreads);
	}
	
	/**
	 * To be run when all branches are complete
	 */
	private void asyncDone() {
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
		Point point = game.getBoard().position(bestindex);
		game.commitMove(this, point.y, point.x);
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






