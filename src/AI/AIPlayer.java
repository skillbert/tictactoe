package AI;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import common.Board;
import common.Game;
import common.Player;

public abstract class AIPlayer implements Player {
	protected Game game;
	protected ArrayList<int[]> wincons;
	protected int myMark;
	private String name;

	public AIPlayer(String name, int mark) {
		this.myMark = mark;
		this.name = name;
	}

	@Override
	public void setGame(Game game) {
		this.game = game;
		game.addObserver(this);
		wincons = (ArrayList<int[]>) game.getBoard().getWinConditions().clone();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getMark() {
		return myMark;
	}

	@Override
	public void showModalMessage(String message) {
	}

	@Override
	public void obtainTurn() {
		Point move = thinkMove(game.getBoard());
		game.commitMove(this, move.y, move.x);
		int a = 0;
	}

	@Override
	public void update(Observable o, Object arg) {
	}

	public abstract Point thinkMove(Board board);
}
