package server;

import java.nio.channels.AsynchronousSocketChannel;

import common.AsyncSocket;
import common.SessionState;

public class ClientConnection {
	private String name;
	private SessionState state;
	private AsyncSocket sock;
	private Server server;

	public ClientConnection(Server server, AsynchronousSocketChannel asyncChannel, String name) {
		this.server = server;
		this.name = name;
		this.state = SessionState.lobby;

		sock = AsyncSocket.create(asyncChannel);
		sock.onClose(() -> server.disconnectClient(this));
		sock.onMessage(str -> parseMessage(str));
	}

	private void parseMessage(String message) {
		System.out.println("received string from client: " + message);
		String[] parts = message.split(" ");
		switch (parts[0]) {
		case "queue":
			if (state != SessionState.lobby) {
				showModalMessage("You need to be in the lobby to queue");
				break;
			}
			setState(SessionState.queued);
			server.findQueue();
			break;
		}
	}

	public void disconnect() {
		sock.close();
	}

	public String getName() {
		return name;
	}

	public SessionState getState() {
		return state;
	}

	public void setState(SessionState state) {
		this.state = state;
		sock.sendString("sessionstate " + state);
	}

	public void showModalMessage(String message) {
		sock.sendString("message " + message);
	}

	public void sendString(String str) {
		sock.sendString(str);
	}

}












