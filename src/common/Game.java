package common;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

public class Game extends Observable {
	public static final int NUMBER_PLAYERS = 2;
	private GameState state;
	private Board board;
	private ArrayList<? extends Player> players;
	private int turn;
	private Point lastmove;

	public Game(ArrayList<? extends Player> players) {
		if (players.size() != 2) {
			throw new RuntimeException("nope, max 2 players for now");
		}
		board = new Board();
		this.players = players;
		reset();
	}

	private void reset() {
		board.reset();
		turn = 0;
		state = GameState.onGoing;
		lastmove = new Point(0, 0);
	}

	public void startGame() {
		setChanged();
		notifyObservers(EventType.started);
	}

	public void commitMove(Player player, int row, int column) {
		if (!hasTurn(player)) {
			player.showModalMessage("It is not your turn!");
			return;
		}
		if (state != GameState.onGoing) {
			player.showModalMessage("This game is over, you can no longer place pieces.");
			return;
		}
		int index = board.indexFromColumn(row, column);
		if (index == Board.INVALID_INDEX) {
			player.showModalMessage("The column you selected is full");
			return;
		}
		board.setField(index, player.getMark());
		lastmove = new Point(column, row);
		turn++;

		int winmark = board.findWinner();
		if (winmark != Mark.EMPTY) {
			// Player winner = players.stream().filter(p -> p.getMark() ==
			// winmark).findAny().get();
			state = GameState.won;
		} else if (board.isFull()) {
			state = GameState.draw;
		}
		setChanged();
		notifyObservers(EventType.placed);
		if (winmark == Mark.EMPTY) {
			getTurn().obtainTurn();
		}
	}

	public Player getTurn() {
		return players.get(turn % players.size());
	}

	public Player getPreviousTurn() {
		return players.get((turn + players.size() - 1) % players.size());
	}

	public GameState getState() {
		return state;
	}

	public Point getLastMove() {
		return lastmove;
	}

	private boolean hasTurn(Player player) {
		return getTurn() == player;
	}

	public ArrayList<? extends Player> getPlayers() {
		return players;
	}

	public static enum GameState {
		onGoing, won, draw
	}

	public static enum EventType {
		started, placed
	}

	public Board getBoard() {
		return board;
	}

	@Override
	public String toString() {
		String str = board.toString();
		str += "It is " + getTurn().getName() + "'s turn";
		return str;
	}
}





