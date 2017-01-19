package client;

import java.io.IOException;
import java.util.Observable;

import common.AsyncSocket;
import common.Game;
import common.SessionState;
import common.Ui;

public class Session extends Observable {

	private AsyncSocket sock;
	private SessionState state;
	private Game currentGame;
	private Ui ui;

	public Session() {
		setState(SessionState.disconnected);
		ui = new Tui(this);
		this.addObserver(ui);
	}

	public void run() {
		ui.run();
	}

	public SessionState getState() {
		return state;
	}

	public void setState(SessionState state) {
		this.state = state;
		setChanged();
		notifyObservers(Ui.UpdateType.state);
	}

	public void connect(String host, int port) {
		setState(SessionState.connecting);
		sock = AsyncSocket.create();
		sock.onClose(() -> connectionClosed());
		sock.onMessage(str -> parseMessage(str));
		sock.onConnect(() -> connected());
		sock.onConnectFail(() -> connectFailed());
		try {
			sock.connect(host, port);
		} catch (IOException ex) {
			System.out.println("Failed to connect, error: " + ex.getMessage());
			return;
		}
	}

	public void queueGame() {
		if (state != SessionState.lobby) {
			ui.showModalMessage("You need to be in the lobby to queue for a game.");
			return;
		}

		sock.sendString("queue");
	}

	private void connectionClosed() {
		setState(SessionState.disconnected);
		sock = null;
	}

	private void connectFailed() {
		ui.showModalMessage("Failed to connect to the server");
		setState(SessionState.disconnected);
	}

	private void parseMessage(String message) {
		// TODO add some sort of protocol error handling
		String[] parts = message.split(" ");
		switch (parts[0]) {
		case "sessionstate":
			SessionState state = SessionState.valueOf(parts[1]);
			System.out.println(state);
			setState(state);
			break;

		case "message":
			ui.showModalMessage(message.split(" ", 2)[1]);
			break;
		}
	}

	private void connected() {
		setState(SessionState.lobby);
	}
}
















