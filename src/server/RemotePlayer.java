package server;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Observable;

import common.Game;
import common.Game.GameState;
import common.Player;
import common.Protocol;
import common.SessionState;

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
		Game updateGame = (Game) o;
		
		switch ((Game.EventType) arg) {
			case placed:
				String newPlayer = updateGame.getTurn().getName();
				String currentPlayer = updateGame.getPreviousTurn().getName();
				Point lastmove = updateGame.getLastMove();
				connection.sendString("placed " + updateGame.getState() + " " + lastmove.x + " "
						+ lastmove.y + " " + currentPlayer + " " + newPlayer);
				
				if (updateGame.getState() != GameState.onGoing) {
					connection.setState(SessionState.lobby);
					connection.setPlayer(null);
					connection.getServer().broadcastPlayers();
				}
				break;
			
			case started:
				ArrayList<? extends Player> players = updateGame.getPlayers();
				String playerstr = "";
				for (Player p : players) {
					playerstr += " " + p.getName();
				}
				connection.setState(SessionState.ingame);
				if (updateGame.getPlayers().size() == 2 && updateGame.getBoard().getSize() == 4) {
					connection.sendString(Protocol.STARTGAME + playerstr);
				} else {
					connection.sendString(Protocol.STARTCUSTOMGAME + " "
							+ updateGame.getBoard().getSize() + playerstr);
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
























