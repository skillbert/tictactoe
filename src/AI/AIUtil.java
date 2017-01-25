package AI;

import java.util.ArrayList;

import common.Board;

public class AIUtil {
    /**
     * Takes a Board and returns all possible moves for use in AI
     * @param board
     *              Board class to use
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
}
