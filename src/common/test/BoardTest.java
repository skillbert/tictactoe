package common.test;

import org.junit.Test;

import common.Board;

public class BoardTest {
	
	@Test
	public void testBoard() {
		Board board = new Board(4);
		System.out.println(board.toString());
	}
	
}
