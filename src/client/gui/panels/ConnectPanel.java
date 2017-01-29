package client.gui.panels;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import client.command.CommandHandler;
import client.gui.Gui;

public class ConnectPanel extends Panel {
	private Gui gui;
	private JTextField hostTextField;
	private JTextField portTextField;
	
	public ConnectPanel(Gui gui) {
		this.gui = gui;
		
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener((ActionEvent event) -> {
			connect();
		});
			    
	    hostTextField = new JTextField(10);
	    hostTextField.setText("127.0.0.1");
	    portTextField = new JTextField(10);
	    portTextField.setText("12345");
	    
	    panel.add(new JLabel("Host:"), "align right");
	    panel.add(hostTextField);
	    panel.add(new JLabel("Port:"), "align right");
	    panel.add(portTextField);
		panel.add(connectButton, "skip");
	}
	
	private void connect() {
		String[] parts = {"connect", hostTextField.getText(), portTextField.getText()};
		CommandHandler handler = gui.commands.get(parts[0]);
		gui.handleCommand(handler, parts);
	}
}
