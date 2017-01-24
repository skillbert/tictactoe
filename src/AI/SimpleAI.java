package AI;

import java.awt.Point;
import java.util.ArrayList;

import common.Board;
import common.Mark;

/**
 * This algorith assigns scores to every spot in the grid. Points are given for
 * blocking other player's rows and improving own rows
 * 
 * @author Wilbert
 *
 */
public class SimpleAI extends AIPlayer {
	private static final int INSTANTWIN = 1000000;
	private static final int LOSSAFTER = 1000;

	public SimpleAI(String name, int mark) {
		super(name, mark);
	}

	@Override
	public Point thinkMove(Board board) {
		ArrayList<Integer> moves = AIUtil.possibleMoves(board);

		int[] defscores = new int[board.getSize() * board.getSize() * board.getSize()];
		int[] attscores = new int[board.getSize() * board.getSize() * board.getSize()];

		// loop all win conditions in a reverse loop so we can remove indexes
		// from it
		for (int i = wincons.size() - 1; i >= 0; i--) {
			// count the amount of marks every player has in ths row
			int[] markcount = new int[game.getPlayers().size()];
			for (int index : wincons.get(i)) {
				int mark = board.getField(index);
				if (mark != Mark.EMPTY) {
					markcount[mark]++;
				}
			}

			// count the amount of different players that have a mark
			int ninvolved = 0;
			for (int mark = 0; mark < markcount.length; mark++) {
				if (markcount[mark] == 0) {
					continue;
				}
				ninvolved++;
			}

			// remove the wincon if it's dead
			if (ninvolved > 1) {
				System.out.println("removed wincon");
				wincons.remove(i);
				continue;
			}

			// count the value we want to give to this spot
			int defscore = 1;
			int attscore = 1;
			for (int mark = 0; mark < markcount.length; mark++) {
				if (mark == myMark) {
					if (markcount[mark] == board.winLength - 1) {
						attscore += INSTANTWIN;
					}
					attscore += markcount[mark] * markcount[mark];
				} else {
					if (markcount[mark] == board.winLength - 1) {
						defscore += LOSSAFTER;
					}
					defscore += markcount[mark] * markcount[mark];
				}
			}
			// give these points to all involved spots
			for (int index : wincons.get(i)) {
				defscores[index] += defscore;
				attscores[index] += attscore;
			}
		}

		// check what our opponent gets access to the turn after
		// TODO


		// choose the best spot and place
		int best = 0;
		int bestindex = board.INVALID_INDEX;
		for (int index : moves) {
			int score = attscores[index] + defscores[index];
			System.out.println(score);
			if (score > best || bestindex == board.INVALID_INDEX) {
				best = score;
				bestindex = index;
			}
		}
		return board.position(bestindex);
	}

}

























