package server;

import java.util.ArrayList;
import java.util.HashMap;

import common.Player;
import common.Protocol;
import common.RemotePlayer;
import common.SessionState;

public class Invite {
	private static ArrayList<Invite> activeInvites = new ArrayList<>();
	
	private ClientConnection sender;
	private ArrayList<ClientConnection> clients;
	private HashMap<ClientConnection, Boolean> accepts;
	private int boardSize;
	
	/**
	 * Tries to invite the given player names for a game with given dimensions.
	 * The corresponding errors are sent if the invitation fails
	 * 
	 * @param sender
	 *            the client that sent this invitation
	 * @param boardSize
	 *            the size of the board
	 * @param namestr
	 *            a string containing the invited players separated with spaces
	 */
	public static void sendInvite(ClientConnection sender, int boardSize, String namestr) {
		// collect info and validate the invite
		ArrayList<ClientConnection> clients = new ArrayList<>();
		for (String name : namestr.split(" ")) {
			ClientConnection player = sender.getServer().findPlayer(name);
			if (player == null) {
				sender.sendString(Protocol.ERROR + " " + Protocol.E_INVITATIONDENIED + " " + name
						+ " player was not found");
				return;
			}
			if (player.getState() != SessionState.lobby) {
				sender.sendString(Protocol.ERROR + " " + Protocol.E_INVITATIONDENIED + " " + name
						+ " player was not in the lobby");
				return;
			}
			clients.add(player);
		}
		
		// update the state and send everything to the clients
		for (ClientConnection client : clients) {
			client.setState(SessionState.invited);
			client.sendString(Protocol.INVITATION + " " + boardSize + " " + (clients.size() + 1)
					+ " " + sender.getName());
		}
		sender.setState(SessionState.invited);
		clients.add(sender);
		
		Invite invite = new Invite(sender, boardSize, clients);
		activeInvites.add(invite);
		Server.getInstance().broadcastPlayers();
	}
	
	/**
	 * Handles everything related to a reply by a client
	 * 
	 * @param client
	 *            the client which replied
	 * @param accepted
	 *            true if the client accepted, false if he denied
	 */
	public static void replyInvite(ClientConnection client, boolean accepted) {
		Invite invite = findInvite(client);
		if (invite == null) {
			// TODO improvised error, protocol doesn't specify
			client.sendString(Protocol.ERROR + " " + Protocol.E_INVITATIONDENIED + " "
					+ client.getName() + " You don't have any invites to reply to");
			return;
		}
		
		if (accepted) {
			invite.accept(client);
		} else {
			invite.denied(client);
		}
	}
	
	/**
	 * finds an invite corresponding to the given client
	 * 
	 * @param client
	 *            the client to search for
	 * @return returns an invite of null if the player has no invitations
	 */
	public static Invite findInvite(ClientConnection client) {
		for (Invite invite : activeInvites) {
			if (invite.clients.contains(client)) {
				return invite;
			}
		}
		return null;
	}
	
	/**
	 * accepts an invitations and starts a game if all players accepted
	 * 
	 * @param client
	 *            the client that accepted
	 */
	private void accept(ClientConnection client) {
		accepts.put(client, true);
		if (accepts.values().stream().allMatch(accepted -> accepted)) {
			ArrayList<Player> players = new ArrayList<>();
			for (int i = 0; i < clients.size(); i++) {
				players.add(new RemotePlayer(clients.get(i), i));
			}
			
			activeInvites.remove(this);
			Server.getInstance().startGame(boardSize, players);
		}
	}
	
	/**
	 * denies an invitation and sends an error to all other players
	 * 
	 * @param denyer
	 */
	private void denied(ClientConnection denyer) {
		for (ClientConnection client : clients) {
			client.setState(SessionState.lobby);
			if (client != denyer) {
				// TODO not specified by protocol. Sending a denied message to
				// everyone, otherwise the other clients don't know about it
				client.sendString(Protocol.ERROR + " " + Protocol.E_INVITATIONDENIED + " "
						+ denyer.getName() + " A player denied the invitation");
			}
		}
		activeInvites.remove(this);
		Server.getInstance().broadcastPlayers();
	}
	
	/**
	 * Creates a new invite
	 * 
	 * @param sender
	 * @param boardSize
	 * @param clients
	 */
	private Invite(ClientConnection sender, int boardSize, ArrayList<ClientConnection> clients) {
		this.sender = sender;
		this.boardSize = boardSize;
		this.clients = clients;
		
		accepts = new HashMap<>();
		for (ClientConnection client : clients) {
			accepts.put(client, client == sender);
		}
	}
}

















