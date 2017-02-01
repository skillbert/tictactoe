package client.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Observable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import client.Session;
import client.Ui;
import client.command.CommandHandler;
import client.command.Commands;
import client.gui.panels.ConnectPanel;
import client.gui.panels.GamePanel;
import client.gui.panels.LobbyPanel;
import client.gui.panels.LoginPanel;
import client.gui.panels.Panel;
import common.SessionState;
import exceptions.ValidationError;
import net.miginfocom.swing.MigLayout;

public class Gui extends JFrame implements Ui {
	static final long serialVersionUID = 235L;
	
	private JFrame mainFrame;
	private Panel connectPanel;
	private Panel loginPanel;
	private LobbyPanel lobbyPanel;
	private GamePanel gamePanel;
	
	private JLabel modalField;
	private Session session;
	public final Map<String, CommandHandler> commands;
	
	public Gui(Session session) {
		this.session = session;
		
		Commands Commands = new Commands(this.session);
		commands = Commands.commands;
		
		mainFrame = new JFrame("Connect 3D");
		mainFrame.setSize(400, 400);
		mainFrame.setLayout(new MigLayout("wrap 2"));
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		
		modalField = new JLabel();
		// modalField.setFont(new Font("Serif", Font.BOLD, 12));
		modalField.setForeground(Color.RED);
		
		connectPanel = new ConnectPanel(this);
		loginPanel = new LoginPanel(this);
		lobbyPanel = new LobbyPanel(this);
		gamePanel = new GamePanel(this);
	}
	
	/**
	 * Runs the ui on the current thread and blocks until the user leaves the
	 * application
	 */
	@Override
	public void run() {
		showPanel(connectPanel);
	};
	
	public Session getSession() {
		return this.session;
	}
	
	public void showPanel(Panel panel) {
		mainFrame.getContentPane().removeAll();
		mainFrame.getContentPane().add(panel.getPanel());
		mainFrame.add(modalField, "skip");
		
		mainFrame.revalidate();
		mainFrame.repaint();
		mainFrame.setVisible(true);
		
	}
	
	private void gameChanged() {
		gamePanel.refresh();
		mainFrame.revalidate();
		mainFrame.repaint();
	}
	
	/**
	 * Shows a message to the user
	 */
	@Override
	public void showModalMessage(String message) {
		modalField.setText("Error: " + message);
		Timer timer = new Timer(4000, hideModalMessage);
		timer.setRepeats(false);
		timer.start();
	};
	
	ActionListener hideModalMessage = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			modalField.setText("");
		}
	};
	
	@Override
	public void setTitle(String status) {
		mainFrame.setTitle(String.format("Connect 3D | %s", status));
	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println(arg.toString());
		switch ((Ui.UpdateType) arg) {
			case state:
				stateChanged();
				if (session.getState() == SessionState.lobby
						|| session.getState() == SessionState.queued) {
					lobbyPanel.refresh();
				}
				break;
			case gamemove:
				gameChanged();
				break;
			case lobby:
				lobbyPanel.refresh();
				break;
		}
	}
	
	private void stateChanged() {
		SessionState state = session.getState();
		switch (state) {
			case connecting:
				setTitle("Connecting ...");
				break;
			case authenticating:
				showPanel(loginPanel);
				setTitle("Login");
				break;
			case lobby:
				showPanel(lobbyPanel);
				
				setTitle("Lobby");
				break;
			case queued:
				setTitle("Queueing");
				break;
			case disconnected:
				showPanel(connectPanel);
				setTitle("Disconnected");
				break;
			case ingame:
				showPanel(gamePanel);
				setTitle("In-game");
				gameChanged();
				break;
		}
	}
	
	
	public void handleCommand(CommandHandler handler, String[] parts) {
		try {
			handler.validateState();
			handler.validateArgs(parts);
			handler.handle(parts);
		} catch (ValidationError | NumberFormatException e) {
			showModalMessage(e.getMessage());
		}
	}
	
	@Override
	public void showMoveSuggestion(Point point) {
		// TODO Auto-generated method stub
	}
}
