package ai;

import java.awt.Point;

import common.Board;
import common.Game;
import common.Mark;

/**
 * An Ai that recursively tries all moves up to a certain depth
 * 
 * @author Wilbert
 *
 */
public class BruteForceAI extends AIBase {
	private static final int WINVALUE = 100000;
	// board value at which we can assume that there is a win in the board
	protected static final int WINLIMIT = WINVALUE / 2;
	private int winlength;
	private int boardSize;
	protected int maxFieldIndex;
	protected int nPlayers;
	protected int layerSize;
	protected final boolean debugprint = true;
	protected int maxdepth;
	protected int[][][] reverseWins;
	
	public BruteForceAI(Game game, int mark) {
		super(game, mark);
		this.maxdepth = 6;
		this.winlength = game.getBoard().getWinLength();
		this.boardSize = game.getBoard().getSize();
		this.maxFieldIndex = game.getBoard().getFieldLength();
		this.layerSize = boardSize * boardSize;
		this.nPlayers = game.getPlayers().size();
		this.reverseWins = AIUtil.reverseWinconditions(wincons, game.getBoard());
	}
	
	
	@Override
	public Point thinkMove(Board board) {
		int[] fields = board.getFieldsClone();
		int[] moves = AIUtil.boardColumnIndexes(board);
		
		MoveResult result = recursiveMove(fields, moves, myMark, Board.INVALID_INDEX, maxdepth);
		
		return board.position(result.index);
	}
	
	/**
	 * Recursively tries moves and returns the resulting board state if all
	 * players play perfectly from the current board state on
	 * 
	 * @param fields
	 *            the current board state
	 * @param moves
	 *            the available moves
	 * @param turnmark
	 *            the mark that current has the turn
	 * @param changedIndex
	 *            the last changed board index or Board.INVALID_INDEX if the
	 *            previous move should not be counted
	 * @param depth
	 *            the amount of iterations left to go
	 * @return A moveresult object containing the optimal move and the resulting
	 *         board value at the end of the tree
	 */
	protected MoveResult recursiveMove(int[] fields, int[] moves, int turnmark, int changedIndex,
			int depth) {
		int valueChange = 0;
		if (changedIndex != Board.INVALID_INDEX) {
			valueChange = calculateBoardValueChange(fields, changedIndex);
		}
		
		if (depth == 0 || valueChange > WINLIMIT || valueChange < -WINLIMIT) {
			return new MoveResult(changedIndex, valueChange);
		}
		
		int newturnMark = (turnmark + 1) % nPlayers;
		int bestValue = 0;
		int bestmove = -1;
		for (int i = 0; i < layerSize; i++) {
			int move = moves[i];
			if (move == Board.INVALID_INDEX || move >= maxFieldIndex) {
				continue;
			}
			fields[move] = turnmark;
			moves[i] += layerSize;
			int value = recursiveMove(fields, moves, newturnMark, move, depth - 1).score;
			fields[move] = Mark.EMPTY;
			moves[i] -= layerSize;
			
			if (debugprint && depth >= maxdepth) {
				printMove(move, value, depth, turnmark);
			}
			
			if (bestmove == -1 || (turnmark == myMark ? value > bestValue : value < bestValue)) {
				bestValue = value;
				bestmove = move;
			}
		}
		
		return new MoveResult(bestmove, valueChange + bestValue);
	}
	
	/**
	 * debug function to display the internal move scores
	 * 
	 * @param index
	 * @param value
	 * @param depth
	 * @param turnmark
	 */
	protected void printMove(int index, int value, int depth, int turnmark) {
		String str = depth + String.format("%1$" + (13 - depth * 2) + "s", "");
		str += (turnmark == myMark ? "myturn" : "opturn");
		str += " " + ((index / boardSize) % boardSize) + "," + (index % boardSize);
		// str += " " + index;
		str += "\t" + value;
		System.out.println(str);
	}
	
	/**
	 * Calculates a value of the current board state
	 * 
	 * @param fields
	 *            the board
	 * @return the value of the current board state
	 */
	private int calculateBoardValue(int[] fields) {
		int value = 0;
		for (int[] row : wincons) {
			value += calcRowValue(fields, row);
		}
		return value;
	}
	
	/**
	 * Calculates the change in board value between the current state and if the
	 * given field index would be an empty mark
	 * 
	 * @param fields
	 *            the complete board
	 * @param changedindex
	 *            the index that has changed
	 * @return the change in board value
	 */
	protected int calculateBoardValueChange(int[] fields, int changedindex) {
		int value = 0;
		int oldmark = fields[changedindex];
		fields[changedindex] = Mark.EMPTY;
		for (int[] row : reverseWins[changedindex]) {
			value -= calcRowValue(fields, row);
		}
		fields[changedindex] = oldmark;
		for (int[] row : reverseWins[changedindex]) {
			value += calcRowValue(fields, row);
		}
		return value;
	}
	
	/**
	 * calculates the score of a set of fields that represent a row
	 * 
	 * @param fields
	 *            the complete board of the game
	 * @param row
	 *            a set of indexes that together represent a row
	 * @param myMark
	 *            the mark that is considered a positive score
	 * @return returns a score value that indicates the value of this row in
	 *         winning the game
	 */
	private int calcRowValue(int[] fields, int[] row) {
		int rowOwner = -1;
		int rowCount = 0;
		boolean isDead = false;
		for (int index : row) {
			int mark = fields[index];
			if (mark == -1) {
				continue;
			}
			if (mark != rowOwner) {
				if (rowOwner == -1) {
					rowOwner = mark;
				} else {
					isDead = true;
					break;
				}
			}
			if (mark == rowOwner) {
				rowCount++;
			}
		}
		// empty rows or rows with multiple different marks are useless
		if (isDead || rowOwner == -1) {
			return 0;
		}
		
		// a full row is a win (or a loss)
		if (rowCount == winlength) {
			return (rowOwner == myMark ? 1 : -1) * WINVALUE;
		}
		
		// assign a value to different sizes of rows
		int rowValue = rowCount * rowCount;
		if (rowOwner == myMark) {
			return rowValue;
		} else {
			return -rowValue;
		}
	}
	
	/**
	 * Represents an outcome of a certain move
	 * 
	 * @author Wilbert
	 *
	 */
	protected class MoveResult {
		protected int index;
		protected int score;
		
		public MoveResult(int index, int score) {
			this.index = index;
			this.score = score;
		}
	}
}

















