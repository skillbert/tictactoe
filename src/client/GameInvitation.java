package client;

public class GameInvitation {
	private int boardSize;
	private String inviter;
	
	public GameInvitation(int boardSize, String inviter) {
		this.boardSize = boardSize;
		this.inviter = inviter;
	}
	
	public String getIntviter() {
		return inviter;
	}
	
	public int getSize() {
		return boardSize;
	}
}
