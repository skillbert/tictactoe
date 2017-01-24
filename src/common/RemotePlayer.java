package common;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import server.ClientConnection;

public class RemotePlayer implements Player {
	private ClientConnection connection;
	private Mark mark;
	private Game game;

	// TODO merge this into ClientConnection
	public RemotePlayer(ClientConnection connection, Mark mark) {
		this.connection = connection;
		this.mark = mark;
		connection.setPlayer(this);
	}

	@Override
	public String getName() {
		return connection.getName();
	}

	@Override
	public Mark getMark() {
		return mark;
	}

	@Override
	public void showModalMessage(String message) {
		connection.showModalMessage(message);
	}

	@Override
	public void update(Observable o, Object arg) {
		Game game = (Game) o;

		switch ((Game.EventType) arg) {
		case placed:
			String newPlayer = game.getTurn().getName();
			String currentPlayer = game.getPreviousTurn().getName();
			Point lastmove = game.getLastMove();
			connection.sendString("placed " + game.getState() + " " + lastmove.x + " " + lastmove.y + " "
					+ currentPlayer + " " + newPlayer);
			break;

		case started:
			ArrayList<? extends Player> players = game.getPlayers();
			String playerstr = "";
			for (Player p : players) {
				playerstr += " " + p.getName();
			}
			connection.setState(SessionState.ingame);
			connection.sendString("startGame" + playerstr);
			break;
		}
	}

	public ClientConnection getClientConnection() {
		return connection;
	}

	public Game getGame() {
		return game;
	}

	@Override
	public void setGame(Game game) {
		this.game = game;
		game.addObserver(this);
	}
}
























