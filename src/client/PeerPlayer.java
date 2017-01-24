package client;

import java.util.Observable;

import common.Game;
import common.Player;

public class PeerPlayer implements Player {
	private String name;
	private int mark;

	public PeerPlayer(String name, int mark) {
		this.name = name;
		this.mark = mark;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getMark() {
		return mark;
	}

	@Override
	public void showModalMessage(String message) {
	}

	@Override
	public void setGame(Game game) {
	}
}
