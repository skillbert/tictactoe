package AI;

import java.awt.Point;

import common.Board;
import common.Game;
import common.Mark;
import common.Player;

public abstract class AIPlayer implements Player {
	private Game game;
	private Mark mark;
	private String name;

	public AIPlayer(String name, Mark mark) {
		this.mark = mark;
		this.name = name;
	}

	@Override
	public void setGame(Game game) {
		this.game = game;
		game.addObserver(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Mark getMark() {
		return mark;
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


	public abstract Point thinkMove(Board board);
}
