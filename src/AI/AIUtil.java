package AI;

import java.util.ArrayList;

import common.Board;

public class AIUtil {
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
