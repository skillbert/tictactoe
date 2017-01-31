package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import common.Board;
import common.Mark;

public class BoardTest {
	
	@Test
	public void testFields() {
		Board board = new Board(4);
		assertEquals(board.indexFromColumn(0, 0), board.index(0, 0, 0));
		board.setField(board.index(0, 0, 0), Mark.YELLOW);
		assertEquals(board.indexFromColumn(0, 0), board.index(0, 0, 1));
		assertEquals(board.getField(board.index(0, 0, 0)), Mark.YELLOW);
		assertFalse(board.isFull());
		board.setField(board.index(0, 0, 0), Mark.BLUE);
		assertEquals(board.getSize(), 4);
		assertEquals(board.getWinLength(), 4);
		assertEquals(board.getFieldLength(), 4 * 4 * 4);
	}
	
	@Test
	public void testWinner() {
		Board board = new Board(4);
		board.setField(board.index(0, 0, 0), Mark.YELLOW);
		board.setField(board.index(1, 0, 0), Mark.YELLOW);
		board.setField(board.index(2, 0, 0), Mark.YELLOW);
		assertEquals(board.findWinner(), Mark.EMPTY);
		board.setField(board.index(3, 0, 0), Mark.YELLOW);
		assertEquals(board.findWinner(), Mark.YELLOW);
	}
	
	@Test
	public void testBounds() {
		Board board = new Board(4);
		assertFalse(board.isField(-1, 0, 0));
		assertFalse(board.isField(0, 4, 0));
		assertFalse(board.isField(-1));
		assertFalse(board.isField(4 * 4 * 4));
		
		assertTrue(board.isField(0, 0, 0));
		assertTrue(board.isField(3, 3, 3));
	}
	
	@Test
	public void testPlacing() {
		Board board = new Board(4);
		assertFalse(board.isAvailableField(board.index(0, 0, 1)));
		assertTrue(board.isAvailableField(board.index(0, 0, 0)));
		board.setField(board.index(0, 0, 0), Mark.BLUE);
		assertTrue(board.isAvailableField(board.index(0, 0, 1)));
	}
}
