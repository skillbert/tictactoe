package ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.AsyncSocket.Callback1;
import common.Board;
import common.Game;
import exceptions.UnknownAIException;

public abstract class AIBase {
	protected Game game;
	protected ArrayList<int[]> wincons;
	protected int myMark;
	protected Callback1<Point> onDone;
	private static Map<String, AIType> aiTypes;
	private static Map<AIType, String> aiNames;
	
	static {
		aiTypes = new HashMap<>();
		aiTypes.put("easy", AIType.random);
		aiTypes.put("medium", AIType.simple);
		aiTypes.put("hard", AIType.bruteforce);
		aiNames = new HashMap<>();
		aiNames.put(AIType.random, "Dumb_bot");
		aiNames.put(AIType.simple, "OK_bot");
		aiNames.put(AIType.bruteforce, "GGWP_bot");
	}
	
	
	public static Map<AIType, String> getAiNames() {
		return aiNames;
	}
	
	public static Map<String, AIType> getAiTypes() {
		return aiTypes;
	}
	
	public static AIBase getAi(Game game, int mark, AIType aiType) {
		switch (aiType) {
			case random:
				return new RandomAi(game, mark);
			case simple:
				return new SimpleAI(game, mark);
			case bruteforce:
				return new ThreadedBruteforceAI(game, mark);
			default:
				throw new IllegalArgumentException();
		}
	}
	
	public static void startGetSingleMove(Game game, int mark, AIType aiType,
			Callback1<Point> donefunc) throws UnknownAIException {
		AIBase ai = getAi(game, mark, aiType);
		ai.startThinkMove(donefunc);
	}
	
	/**
	 * Initializes an AIPlayer with name and mark
	 * 
	 * @param name
	 *            AIPlayer name to use
	 * @param mark
	 *            AIPlayer mark to use
	 */
	public AIBase(Game game, int mark) {
		this.myMark = mark;
		this.game = game;
		this.wincons = (ArrayList<int[]>) game.getBoard().getWinConditions().clone();
	}
	
	public void startThinkMove(Callback1<Point> donefunc) {
		this.onDone = donefunc;
		Thread thread = new Thread(() -> thinkThread());
		thread.start();
	}
	
	private void thinkThread() {
		Point move = thinkMove(game.getBoard());
		onDone.run(move);
	}
	
	/**
	 * Performs the calculations to determine the move to make and returns the
	 * move.
	 * 
	 * @param board
	 *            Board object to use
	 * @return the move to make as a Point object or null if the thinking will
	 *         be done async
	 */
	protected abstract Point thinkMove(Board board);
}
