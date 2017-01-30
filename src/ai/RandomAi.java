package ai;

import java.awt.Point;
import java.util.ArrayList;

import common.Board;
import common.Game;

public class RandomAi extends AIBase {
	
	public RandomAi(Game game, int mark) {
		super(game, mark);
	}
	
	@Override
	public Point thinkMove(Board board) {
		ArrayList<Integer> moves = AIUtil.possibleMoves(board);
		return board.position(moves.get((int) (Math.random() * moves.size())));
	}
}
