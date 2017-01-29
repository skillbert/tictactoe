package ai;

import java.awt.Point;
import java.util.ArrayList;

import common.Board;

public class RandomAi extends AIPlayer {
	
	public RandomAi(String name, int mark) {
		super(name, mark);
	}
	
	@Override
	public Point thinkMove(Board board) {
		ArrayList<Integer> moves = AIUtil.possibleMoves(board);
		return board.position(moves.get((int) (Math.random() * moves.size())));
	}
}
