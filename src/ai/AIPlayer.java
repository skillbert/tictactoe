package ai;

import java.util.Observable;

import common.Game;
import common.Player;

public class AIPlayer implements Player {
	protected Game game;
	private int myMark;
	private String name;
	private AIType aiType;
	private AIBase ai;
	
	/**
	 * Initializes an AIPlayer with name and mark.
	 * 
	 * @param name
	 *            AIPlayer name to use
	 * @param mark
	 *            AIPlayer mark to use
	 */
	public AIPlayer(String name, int mark, AIType type) {
		this.myMark = mark;
		this.name = name;
		this.aiType = type;
	}
	
	@Override
	public void setGame(Game game) {
		this.ai = AIBase.getAi(game, myMark, aiType);
		this.game = game;
		game.addObserver(this);
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
		ai.startThinkMove(move -> game.commitMove(this, move.y, move.x));
	}
	
	@Override
	public void update(Observable o, Object arg) {
	}
}
