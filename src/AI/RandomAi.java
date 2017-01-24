package AI;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import common.Board;
import common.Mark;

public class RandomAi extends AIPlayer {

	public RandomAi(String name, Mark mark) {
		super(name, mark);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Point thinkMove(Board board) {
		ArrayList<Point> moves = new ArrayList<>();
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				if (board.indexFromColumn(y, x) != Board.INVALID_INDEX) {
					moves.add(new Point(x, y));
				}
			}
		}
		return moves.get((int) (Math.random() * moves.size()));
	}
}
