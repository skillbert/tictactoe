package AI;

import java.awt.Point;
import java.util.ArrayList;

import common.Board;

public class SimpleAI extends AIPlayer {
	private ArrayList<int[]> wincons;

	public SimpleAI(String name, int mark) {
		super(name, mark);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Point thinkMove(Board board) {
		ArrayList<Integer> moves = AIUtil.possibleMoves(board);

		for (int[] win : wincons) {
			int[] scores = new int[game.getPlayers().size()];
			for (int index : win) {
				int m = board.getField(index);
			}
		}

		return new Point(0, 0);

	}

}


















