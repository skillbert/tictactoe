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

	public void showModalMessage(String message);
}
