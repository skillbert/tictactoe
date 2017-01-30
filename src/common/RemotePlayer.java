package common;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import common.Game.GameState;
import server.ClientConnection;

public class RemotePlayer implements Player {
	private ClientConnection connection;
	private int mark;
	private Game game;
	
	// TODO merge this into ClientConnection
	public RemotePlayer(ClientConnection connection, int mark) {
		this.connection = connection;
		this.mark = mark;
		connection.setPlayer(this);
	}
	
	@Override
	public String getName() {
		return connection.getName();
	}
	
	@Override
	public int getMark() {
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
				connection.sendString("placed " + game.getState() + " " + lastmove.x + " "
						+ lastmove.y + " " + currentPlayer + " " + newPlayer);
				
				if (game.getState() != GameState.onGoing) {
					connection.setState(SessionState.lobby);
					connection.setPlayer(null);
				}
				break;
			
			case started:
				ArrayList<? extends Player> players = game.getPlayers();
				String playerstr = "";
				for (Player p : players) {
					playerstr += " " + p.getName();
				}
				connection.setState(SessionState.ingame);
				if (game.getPlayers().size() == 2 && game.getBoard().getSize() == 4) {
					connection.sendString(Protocol.STARTGAME + playerstr);
				} else {
					connection.sendString(
							Protocol.STARTCUSTOMGAME + " " + game.getBoard().getSize() + playerstr);
				}
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
























