package ai;

import java.util.ArrayList;

import common.Board;

public class AIUtil {
	/**
	 * Takes a Board and returns all possible moves for use in AI
	 * 
	 * @param board
	 *            Board class to use
	 * @return ArrayList of all allowed moved.
	 */
	public static ArrayList<Integer> possibleMoves(Board board) {
		ArrayList<Integer> moves = new ArrayList<>();
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				int index = board.indexFromColumn(y, x);
				if (index != Board.INVALID_INDEX) {
					moves.add(index);
				}
			}
		}
		return moves;
	}
	
	/**
	 * Roughly the same as possibleMoves, but always returns an array index for
	 * every column of the board. If a column is full the value will be
	 * Board.INVALID_INDEX
	 * 
	 * @param board
	 * @return returns a size*size array with indexes of the top most free space
	 *         of every column or Board.INVALID_INDEX instead if the column is
	 *         full
	 */
	public static int[] boardColumnIndexes(Board board) {
		int[] moves = new int[board.getSize() * board.getSize()];
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				moves[x * board.getSize() + y] = board.indexFromColumn(y, x);
			}
		}
		return moves;
	}
	
	/**
	 * Calculates the reverse mapping of a wincons structure. This allows for
	 * fast lookup of affected wincons by lookup of a move index
	 * 
	 * @param wincons
	 *            the original wincons structure returned from board
	 * @param board
	 *            the board
	 * @return returns an array with every index containing a list of wincons
	 *         that contain the given index
	 */
	public static int[][][] reverseWinconditions(ArrayList<int[]> wincons, Board board) {
		int[][][] result = new int[board.getFieldLength()][][];
		for (int index = 0; index < board.getFieldLength(); index++) {
			ArrayList<int[]> wins = new ArrayList<>();
			for (int[] win : wincons) {
				boolean contained = false;
				for (int i : win) {
					if (i == index) {
						contained = true;
					}
				}
				if (contained) {
					wins.add(win);
				}
			}
			result[index] = wins.toArray(new int[wins.size()][board.getWinLength()]);
		}
		return result;
	}
}
















