package client.gui.panels;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.command.CommandHandler;
import client.gui.Gui;
import common.Board;
import common.Game;
import common.Mark;
import common.Protocol;

public class GamePanel extends Panel {
	private GridLayout buttonLayout;

	private JButton button;
	private JLabel whoseTurn;
	private JPanel buttonPanel;
	private Map<JButton, Point> buttons = new HashMap<JButton, Point>();
	private List<JButton> buttonsl = new ArrayList<JButton>();
	private int DIM = Protocol.DIM;
	
	public GamePanel(Gui gui) {
		this.gui = gui;
		this.whoseTurn = new JLabel();
		this.buttonLayout = new GridLayout(DIM, DIM);
		buttonPanel = new JPanel(buttonLayout);
		for(int row = 0; row < DIM; row++) {
			for(int col = 0; col < DIM; col++) {
				final Integer innerRow = new Integer(row); // workaround to allow row and col be used in a thread (has to be final)
				final Integer innerCol = new Integer(col);
				button = new JButton();
				button.addActionListener((ActionEvent event) -> {
					place(innerRow, innerCol);
				});
				buttons.put(button, new Point(innerCol, innerRow));
				buttonsl.add(button);
				buttonPanel.add(button);
			}
		}
		panel.add(whoseTurn, "skip");
		panel.add(buttonPanel, "span 2");
	}
	
	public void refresh() {
		panel.removeAll();
		buttonPanel.removeAll();
		Game game = this.gui.getSession().getGame();
		Board board = game.getBoard();
		whoseTurn.setText(String.format("%s turn", game.getTurn().getName()));
		panel.add(whoseTurn, "skip");

		for(JButton button:buttonsl) {
			Point location = buttons.get(button);
			String str = "";
			for (int height = 0; height < DIM; height++) {
				str += Mark.getMarkString(board.getField(board.index(location.y, location.x, height)));
			}
			button.setText(str);
			System.out.println(str);
			buttonPanel.add(button);
		}
		panel.add(buttonPanel, "span 2");
		panel.revalidate();
		panel.repaint();
	}
	
	public void place(int row, int col) {
		String[] parts = {"place", String.valueOf(col+1), String.valueOf(row+1)};
		CommandHandler handler = gui.commands.get(parts[0]);
		gui.handleCommand(handler, parts);
	}
	
}
