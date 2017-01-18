package server;

import common.*;

public class ClientConnection {
	private String name;
	private PlayerState state;
	// private Socket socket

	public ClientConnection(String name) {
		this.name = name;
		this.state = PlayerState.lobby;
	}

	public String getName() {
		return name;
	}

	public PlayerState getState() {
		return state;
	}

	public void setState(PlayerState state){
		this.state=state;
	}
}
