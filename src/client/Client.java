package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import common.*;
import java.util.Observable;

public class Client extends Observable {
	private static Client instance;
	private Socket connection;
	private InputStream in;
	private OutputStream out;
	private PlayerState state;
	private Game currentGame;
	private Ui ui;

	public void Main(String[] args) {
		instance = new Client();
	}
	public static Client getInstance() {
		return instance;
	}

	public Client() {
		state = PlayerState.disconnected;
		ui = new Tui();
	}

	public PlayerState getState() {
		return state;
	}

	public void Connect(String host, int port) {
		try {
			connection = new Socket(host, port);
			in = connection.getInputStream();
			out = connection.getOutputStream();
		} catch (IOException ex) {
			System.out.println("Failed to connect, error: " + ex.getMessage());
			return;
		}
		state = PlayerState.connecting;
		notifyObservers("state");
	}
}
