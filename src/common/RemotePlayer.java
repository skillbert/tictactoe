package common;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import server.ClientConnection;

public class RemotePlayer implements Player {
	private ClientConnection connection;
	private Mark mark;

	public RemotePlayer(ClientConnection connection, Mark mark) {
		this.connection = connection;
		this.mark = mark;
	}

	@Override
	public int determineMove(Board board) {
		// TODO Auto-generated method stub
		return 0;
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
			ArrayList<Player> players = game.getPlayers();
			String playerstr = "";
			for (Player p : players) {
				playerstr += " " + p.getName();
			}
			connection.sendString("startGame" + playerstr);
			break;
		}
	}

	@Override
	public void makeMove(Board board) {
		// TODO Auto-generated method stub

	}
}
























