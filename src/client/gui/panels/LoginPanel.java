package client.gui.panels;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import client.command.CommandHandler;
import client.gui.Gui;

public class LoginPanel extends Panel {
	private Gui gui;
	private JTextField nameTextField;
	
	public LoginPanel(Gui gui) {
		this.gui = gui;
		
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener((ActionEvent event) -> {
			login();
		});
		
		nameTextField = new JTextField(10);
		panel.add(new JLabel("Name:"), "align right");
		panel.add(nameTextField);
		panel.add(loginButton, "skip");
	}
	
	private void login() {
		String[] parts = {"login", nameTextField.getText()};
		CommandHandler handler = gui.commands.get(parts[0]);
		gui.handleCommand(handler, parts);
	}
	
}
