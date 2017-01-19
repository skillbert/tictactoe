package common;

import java.util.Observable;

public class Game extends Observable {
	public static final int NUMBER_PLAYERS = 2;
	private Board board;
	private Player[] players;
	private int turn;


	public Game(Player[] players) {
		if (players.length != 2) {
			throw new RuntimeException("nope, max 2 players for now");
		}
		board = new Board();
		this.players = players;
		startGame();
	}

	public void startGame() {
		turn = 0;
		board.reset();
	}

	public void play() {
		while (!board.gameOver()) {
			update();
			Player player = players[turn % NUMBER_PLAYERS];
			board.setField(player.determineMove(board), player.getMark());
			turn += 1;
		}
		if (board.isWinner(players[(turn - 1) % NUMBER_PLAYERS].getMark())) {
			System.out.println(players[(turn - 1) % NUMBER_PLAYERS].getName() + " Wins");
		} else {
			System.out.println("Board full, no winners.");
		}
	}

	private void moveMade(int index) {
		if (board.gameOver()) {
			setChanged();
			notifyObservers("gameover");
		}
	}

	private void update() {
		setChanged();
		notifyObservers();
	}
}





