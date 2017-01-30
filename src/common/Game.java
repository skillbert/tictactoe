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
	
	/**
	 * Initialize Game, by creating a new board, assigning the players to the
	 * Game, calling reset to set the initial values.
	 * 
	 * @param players
	 *            list of objects implementing Player interface
	 */
	public Game(int boardSize, ArrayList<? extends Player> players) {
		board = new Board(boardSize);
		this.players = players;
		reset();
	}
	
	/**
	 * Sets the initial values of the Game.
	 */
	private void reset() {
		board.reset();
		turn = 0;
		state = GameState.onGoing;
		lastmove = new Point(0, 0);
	}
	
	/**
	 * Start the game
	 */
	public void startGame() {
		setChanged();
		notifyObservers(EventType.started);
	}
	
	/**
	 * Executes a move if it's the players turn, the game is ongoing, the chosen
	 * position is empty and available. if the move is successful it also checks
	 * for a winner and if the board is full. if the game is not over it calls
	 * obtainTurn() on the next player.
	 * 
	 * @param player
	 *            Player that executes the move
	 * @param row
	 *            chosen row
	 * @param column
	 *            chosen column
	 *
	 */
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
			state = GameState.won;
		} else if (board.isFull()) {
			state = GameState.draw;
		}
		setChanged();
		notifyObservers(EventType.placed);
		if (state == GameState.onGoing) {
			getTurn().obtainTurn();
		}
	}
	
	/**
	 * returns the player whose turn it is next turn.
	 * 
	 * @return next player
	 */
	public Player getTurn() {
		return players.get(turn % players.size());
	}
	
	/**
	 * returns the player whose turn it was previous turn.
	 * 
	 * @return previous player
	 */
	public Player getPreviousTurn() {
		return players.get((turn + players.size() - 1) % players.size());
	}
	
	/**
	 * getter game state.
	 * 
	 * @return this.state
	 */
	public GameState getState() {
		return state;
	}
	
	/**
	 * getter last move
	 * 
	 * @return this.lastmove
	 */
	public Point getLastMove() {
		return lastmove;
	}
	
	/**
	 * Checks if it's the given player's turn.
	 * 
	 * @param player
	 *            player object to check
	 * @return if it's the turn of player
	 */
	private boolean hasTurn(Player player) {
		return getTurn() == player;
	}
	
	/**
	 * getter players list
	 * 
	 * @return this.players
	 */
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
		if (state == GameState.won) {
			str += getPreviousTurn().getName() + " won!";
		} else if (state == GameState.draw) {
			str += "The game is a draw!";
		} else {
			str += "It is " + getTurn().getName() + "'s turn";
		}
		return str;
	}
}





