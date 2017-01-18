package common.test;

import static org.junit.Assert.*;

import org.junit.Test;

import common.Board;

public class BoardTest {

    @Test
    public void testBoard() {
        Board board = new Board();
        System.out.println(board.toString());
    }

}
