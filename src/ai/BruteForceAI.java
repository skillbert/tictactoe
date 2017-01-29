package ai;

import java.awt.Point;

import common.Board;
import common.Game;
import common.Mark;

public class BruteForceAI extends AIPlayer {
	private static final int WINVALUE = 100000;
	// board value at which we can assume that there is a win in the board
	private static final int WINLIMIT = WINVALUE / 2;
	private int winlength;
	private int maxFieldIndex;
	private int boardSize;
	private int nPlayers;
	private int layerSize;
	private int resultMove;
	private final boolean debugprint = true;
	private int maxdepth;
	private int[][][] reverseWins;
	
	public BruteForceAI(String name, int mark) {
		super(name, mark);
	}
	
	@Override
	public void setGame(Game game) {
		super.setGame(game);
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
		
		maxdepth = 6;
		recursiveMove(fields, moves, myMark, Board.INVALID_INDEX, maxdepth);
		return board.position(resultMove);
	}
	
	private int recursiveMove(int[] fields, int[] moves, int turnmark, int changedIndex,
			int depth) {
		int valueChange = 0;
		if (changedIndex != Board.INVALID_INDEX) {
			valueChange = calculateBoardValueChange(fields, changedIndex);
		}
		
		if (depth == 0 || valueChange > WINLIMIT || valueChange < -WINLIMIT) {
			return valueChange;
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
			int value = recursiveMove(fields, moves, newturnMark, move, depth - 1);
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
		
		// TODO
		// this is a hack to get a second return value without instantiating a
		// new object at every function iteration (+-100m times)
		// I'm not completely sure what's actually faster
		resultMove = bestmove;
		return valueChange + bestValue;
	}
	
	private void printMove(int index, int value, int depth, int turnmark) {
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
	private int calculateBoardValueChange(int[] fields, int changedindex) {
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
}

















