package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Test;

import client.PeerPlayer;
import common.Game;
import common.Game.GameState;
import common.Mark;
import common.Player;

public class GameTest {
	@Test
	public void testFlow() {
		ArrayList<PeerPlayer> players = new ArrayList<>();
		players.add(new PeerPlayer("bob", Mark.BLUE));
		players.add(new PeerPlayer("mark", Mark.YELLOW));
		Game game = new Game(4, players);
		
		game.startGame();
		
		Player turn1 = game.getTurn();
		assertTrue(game.hasTurn(turn1));
		game.commitMove(turn1, 0, 0);
		assertEquals(game.getPreviousTurn(), turn1);
		Player turn2 = game.getTurn();
		assertTrue(turn1 != turn2);
		assertTrue(game.getLastMove().equals(new Point(0, 0)));
		
		game.commitMove(turn2, 1, 0);
		game.commitMove(turn1, 0, 1);
		game.commitMove(turn2, 1, 1);
		game.commitMove(turn1, 0, 2);
		game.commitMove(turn2, 1, 2);
		assertEquals(game.getState(), GameState.onGoing);
		game.commitMove(turn1, 0, 3);
		assertEquals(game.getState(), GameState.won);
	}
}
