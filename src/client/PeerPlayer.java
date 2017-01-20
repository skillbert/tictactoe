package client;

import java.util.Observable;

import common.Mark;
import common.Player;

public class PeerPlayer implements Player {
	private String name;
	private Mark mark;

	public PeerPlayer(String name, Mark mark) {
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
	public Mark getMark() {
		return mark;
	}

	@Override
	public void showModalMessage(String message) {
	}
}
