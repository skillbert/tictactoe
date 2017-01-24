package common;

import java.util.Observer;

public abstract interface Player extends Observer {
	public String getName();

	/**
	 * @deprecated store this in the game instead?
	 * @return
	 */
	@Deprecated
	public Mark getMark();

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
	 * Called when this player obtains the turn
	 */
	public default void obtainTurn() {
	}
}
