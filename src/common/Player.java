package common;

import java.util.Observer;

public abstract interface Player extends Observer {
    /**
     * getter player name / this.name
     * @return player name
     */
	public String getName();

	/**
	 * @deprecated store this in the game instead? -> set while initializing object, stored on the player?
	 * @return
	 */
	@Deprecated
	public int getMark() {
	}

	/**
	 * Used to display a message to the player
	 * 
	 * @param message
	 *            the message to show
	 */
	public void showModalMessage(String message);

	/**
	 * Connects this player with a game
	 * 
	 * @param game
	 */
	public void setGame(Game game);

	/**
	 * Called when it's this players turn to determine the move. -> why not let the function return the move and execute the move somewhere else?
	 */
	public default void obtainTurn() {
	}
}
