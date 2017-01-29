package client;

import java.util.Observer;

public interface Ui extends Observer {
	public static enum UpdateType {
		state, gamemove, lobby
	}

	/**
	 * Shows a message to the user
	 */
	public abstract void showModalMessage(String message);

	/**
	 * Runs the ui on the current thread and blocks until the user leaves the
	 * application
	 */
	public abstract void run();
}
