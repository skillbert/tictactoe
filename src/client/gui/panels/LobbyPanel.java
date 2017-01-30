package client.gui.panels;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import client.command.CommandHandler;
import client.gui.Gui;

public class LobbyPanel extends Panel {
	private Gui gui;
	private DefaultTableModel playerTableModel;
	private JButton queueButton;
	private JButton cancelQueueButton;
	
	public LobbyPanel(Gui gui) {
		this.gui = gui;
		queueButton = new JButton("Queue");
		cancelQueueButton = new JButton("Cancel queue");
		
		queueButton.addActionListener((ActionEvent event) -> {
			queue();
		});
		cancelQueueButton.addActionListener((ActionEvent event) -> {
			cancelQueue();
		});
		
		Map<String, String> playerLobbyData = gui.getSession().getPlayerLobbyData();
		playerTableModel = toTableModel(playerLobbyData);
		JTable playerTable = new JTable(playerTableModel);
		panel.add(new JScrollPane(playerTable), "span 2");
		panel.add(queueButton);
		panel.add(cancelQueueButton);

	}
		
	public void refresh() {
		panel.removeAll();
		panel.setLayout(layout);
		Map<String, String> playerLobbyData = gui.getSession().getPlayerLobbyData();
		playerTableModel = toTableModel(playerLobbyData);
		JTable playerTable = new JTable(playerTableModel);
		panel.add(new JScrollPane(playerTable), "span 2");
		panel.add(queueButton);
		panel.add(cancelQueueButton);
		panel.revalidate();
		panel.repaint();
	}
	
	public static DefaultTableModel toTableModel(Map<?,?> map) {
	    DefaultTableModel model = new DefaultTableModel(
	        new Object[] { "name", "status" }, 0
	    );
	    for (Map.Entry<?,?> entry : map.entrySet()) {
	        model.addRow(new Object[] { entry.getKey(), entry.getValue() });
	    }
	    return model;
	}
	
	private void queue() {
		String[] parts = {"queue"};
		CommandHandler handler = gui.commands.get(parts[0]);
		gui.handleCommand(handler, parts);
	}
	
	private void cancelQueue() {
		String[] parts = {"cancel_queue"};
		CommandHandler handler = gui.commands.get(parts[0]);
		gui.handleCommand(handler, parts);
	}
}
