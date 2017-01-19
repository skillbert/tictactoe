package common;

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
	public void makeMove(Board board) {

	}

	public ClientConnection getConnection() {
		return connection;
	}
}
