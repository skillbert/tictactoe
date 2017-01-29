package client.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Observable;

import javax.swing.JFrame;
import javax.swing.JLabel;


import client.Session;
import client.Ui;
import client.command.CommandHandler;
import client.command.Commands;

import client.gui.panels.ConnectPanel;
import client.gui.panels.LobbyPanel;
import client.gui.panels.LoginPanel;

import common.SessionState;
import net.miginfocom.swing.MigLayout;
import client.gui.panels.Panel;

public class Gui extends JFrame implements Ui {
	private JFrame mainFrame;
	private Panel connectPanel;
	private Panel loginPanel;
	private Panel lobbyPanel;
	
	private JLabel errorField;
	private JLabel statusField;
	private JLabel modalField;
	private Session session;
	public final Map<String, CommandHandler> commands;

	public Gui(Session session) {
		this.session = session;
		
		Commands Commands = new Commands(this.session);
		commands = Commands.commands;
	
		setTitle("Connect3D");
		mainFrame = new JFrame("Connect 3D");
	    mainFrame.setSize(400,400);
	    mainFrame.setLayout(new MigLayout("wrap 2"));
	    mainFrame.addWindowListener(new WindowAdapter() {
	    		public void windowClosing(WindowEvent windowEvent){
	    			System.exit(0);
	    		}        
	    });
	    
	    errorField = new JLabel();
	    statusField = new JLabel();
	    modalField = new JLabel();

	    connectPanel = new ConnectPanel(this);
		loginPanel = new LoginPanel(this);
		lobbyPanel = new LobbyPanel(this);
		

		showPanel(connectPanel);

	}
	/**
	 * Runs the ui on the current thread and blocks until the user leaves the
	 * application
	 */
	public void run() {
	};
	
	public Session getSession() {
		return this.session;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println(arg.toString());
		switch ((Ui.UpdateType) arg) {
			case state:
				stateChanged();
				break;
			case gamemove:
				gameChanged();
				break;
			case lobby:
				System.out.println("lobbypanel");
				showPanel(lobbyPanel);
				break;
		}
	}
	
	public void showPanel(Panel panel) {
		mainFrame.getContentPane().removeAll();
		mainFrame.getContentPane().add(panel.getPanel());
		showFooter();
	}
	
	
	public void showFooter() {
		mainFrame.add(errorField, "skip");
		mainFrame.add(statusField, "skip");
		mainFrame.add(modalField, "skip");
		mainFrame.setVisible(true);  
	}
	
	/**
	 * Shows a message to the user
	 */
	@Override
	public void showModalMessage(String message) {
		modalField.setText(message);
	};
	
	private void stateChanged() {
		SessionState newstate = session.getState();
		switch (newstate) {
			case connecting:
				statusField.setText("Connecting to server...");
				break;
			case authenticating:
				showPanel(loginPanel);
				statusField.setText("Connected, please choose a name");
				break;
			case lobby:
				showPanel(lobbyPanel);
				statusField.setText("You are now in the lobby");
				break;
			case queued:
				statusField.setText(
						"Queued for random game. A game will start when another player enters the queue.");
				break;
			case disconnected:
				statusField.setText("Disconnected from server");
				break;
			case ingame:
				statusField.setText("entered game");
				gameChanged();
				break;
		}
	}
	
	private void gameChanged() {
		
	}
	
	public void handleCommand(CommandHandler handler, String[] parts) {
		System.out.println(Arrays.toString(parts ) + "Exec");
		System.out.println(handler.toString());
		if (handler.validateState() && handler.validateArgs(parts)) {
			System.out.println(Arrays.toString(parts) + "Validated");

			if (handler.handle(parts)) {
				System.out.println(Arrays.toString(parts) + "Executed");

				return;
			}
		}
		String error = handler.getErrorMessage();
		errorField.setText(error);
	}
	
	public static void main(String[] args){
		Session session = new Session();
		session.run();
	}
}
