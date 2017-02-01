package client;

public class GameInvitation {
	private int boardSize;
	private String inviter;
	
	public GameInvitation(int boardSize, String inviter) {
		this.boardSize = boardSize;
		this.inviter = inviter;
	}
	
	public String getInviter() {
		return inviter;
	}
	
	public int getSize() {
		return boardSize;
	}
}
