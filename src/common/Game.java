package common;

public class Game {
    public static final int NUMBER_PLAYERS = 2;
	private Board board;
	private Player[] players;
	private int turn;
	
	
	public Game(Player p1, Player p2){
		board=new Board();
		players = new Player[NUMBER_PLAYERS];
		players[0] = p1;
		players[1] = p2;
		turn = 0;
	}
	
	public void start() {
	    boolean game_end = false;
	    while (!game_end) {
	        reset();
	        play();
	    }
	}
	    
	public void reset() {
	    turn = 0;
	    board.reset();
	}
	
	public void play() {
	    while(!board.gameOver()) {
	        update();
	        Player player = players[turn%NUMBER_PLAYERS];
	        board.setField(player.determineMove(board), player.getMark());
	        turn += 1;
	    }
	    if(board.isWinner(players[(turn-1)%NUMBER_PLAYERS].getMark())) {
	        System.out.println(players[(turn-1)%NUMBER_PLAYERS].getName() + " Wins");
	    } else {
	        System.out.println("Board full, no winners.");
	    }
	}
	
	private void update() {
	    System.out.println(board.toString());
	}
	
}
