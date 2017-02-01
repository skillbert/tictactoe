package server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;

import ai.AIBase;
import ai.AIPlayer;
import ai.AIType;
import common.AsyncSocket;
import common.CommandParser;
import common.CommandParser.CommandFormatException;
import common.DirectProtocol;
import common.Game;
import common.Mark;
import common.Player;
import common.Protocol;
import common.RemotePlayer;
import common.SessionState;
import common.SocketProtocol;
import httpServer.BasicHttpServer;
import httpServer.WebSocketProtocol;

public class ClientConnection {
	private String name;
	private SessionState state;
	private AsyncSocket sock;
	private Server server;
	private RemotePlayer player;
	private boolean isBasicSocket = true;
	private SocketProtocol socketProtocol;
	
	/**
	 * Initializes a ClientConnection
	 * 
	 * @param server
	 *            Server class to use
	 * @param asyncChannel
	 *            asyncChannel to use
	 * 
	 *            OpenJML on my computer can't typecheck this class even without
	 *            JML.
	 */
	// @ ensures getState() == SessionState.authenticating;
	// @ ensures !isLoggedIn();
	public ClientConnection(Server server, AsynchronousSocketChannel asyncChannel) {
		this.server = server;
		state = SessionState.authenticating;
		this.name = "";
		
		sock = new AsyncSocket(asyncChannel);
		sock.onClose(() -> server.disconnectClient(this));
		sock.onPacket(packet -> parsePacket(packet));
		
		socketProtocol = new DirectProtocol();
		socketProtocol.setSocket(sock);
	}
	
	/**
	 * Parses a raw packet from connection and passes it on to the correct
	 * module
	 * 
	 * @param packet
	 *            the packet to parse
	 */
	private void parsePacket(byte[] packet) {
		// check if the request is using a different protocol
		if (isBasicSocket && state == SessionState.authenticating) {
			// websocket
			WebSocketProtocol websock = WebSocketProtocol.tryConnectWebsocket(packet, sock);
			if (websock != null) {
				socketProtocol = websock;
				isBasicSocket = false;
				System.out.println("websocket client connected");
				return;
			}
			
			// simple http request
			if (BasicHttpServer.tryRespondGET(packet, sock)) {
				return;
			}
		}
		
		String message = socketProtocol.parsePacket(packet);
		if (message != null) {
			parseMessage(message);
		}
	}
	
	/**
	 * Parse a client message according to the protocol
	 * 
	 * @param message
	 *            client message to parse
	 */
	private void parseMessage(String message) {
		System.out.println(name + "\t>> " + message);
		
		CommandParser command = new CommandParser(message);
		try {
			switch (command.getCommand()) {
				case Protocol.LOGIN:
					login(command.nextString());
					break;
				case Protocol.QUEUE:
					queueGame();
					break;
				case Protocol.LEAVEQUEUE:
					unQueueGame();
					break;
				case Protocol.PLACE:
					commitMove(command.nextInt(), command.nextInt());
					break;
				case Protocol.BOT:
					startBotGame(command.nextStringOpt("medium"));
					break;
				case Protocol.LEAVEGAME:
					leaveGame();
					break;
				case Protocol.INVITE:
					Invite.sendInvite(this, command.nextInt(), command.remainingString());
					break;
				case Protocol.REPLY:
					Invite.replyInvite(this, command.nextString().equals("yes"));
					break;
				case Protocol.SPECTATE:
					spectateGame(command.nextString());
					break;
				default:
					sendString(Protocol.ERROR + " " + Protocol.E_UNKNOWNCOMMAND);
					break;
			}
		} catch (CommandFormatException ex) {
			sendString(Protocol.ERROR + " " + Protocol.E_INVALIDCOMMAND);
		}
	}
	
	/**
	 * Parses a request from a client to spectate a game
	 * 
	 * @param playername
	 */
	private void spectateGame(String playername) {
		if (state != SessionState.lobby && state != SessionState.queued) {
			showModalMessage("You need to be in the lobby to spectate a game.");
			return;
		}
		ClientConnection client = server.findPlayer(playername);
		if (client == null) {
			showModalMessage("Can't find " + playername);
			return;
		}
		if (client.getState() != SessionState.ingame) {
			showModalMessage("This player is not in a game");
			return;
		}
		
		Game game = client.getPlayer().getGame();
		RemotePlayer myplayer = new RemotePlayer(this, Mark.EMPTY);
		myplayer.setGame(game);
		player = myplayer;
		state = SessionState.ingame;
		
		// send the board state
		String message = Protocol.GAMESTATE + " " + game.getPlayers().size() + " "
				+ game.getBoard().getSize();
		for (Player player : game.getPlayers()) {
			message += " " + player.getName();
		}
		for (int field : game.getBoard().getFieldsClone()) {
			message += " " + field;
		}
		sendString(message);
	}
	
	/**
	 * Starts a new bot game with the current player and the chosen bot
	 * 
	 * @param botid
	 *            the type of the bot
	 */
	// @ requires getState() == SessionState.lobby || getState() ==
	// SessionState.queued;
	// @ ensures getState() == SessionState.ingame;
	private void startBotGame(String botid) {
		if (state != SessionState.lobby && state != SessionState.queued) {
			showModalMessage("You need to be in the lobby to start a new game");
			return;
		}
		// TODO do something to make sure the bot name is available
		if (!AIBase.getAiTypes().containsKey(botid)) {
			sendString(Protocol.ERROR + " " + Protocol.E_MESSAGE
					+ " Uknown bot type. Bot types are easy, medium and hard.");
			return;
		}
		AIType type = AIBase.getAiTypes().get(botid);
		String botname = AIBase.getAiNames().get(type);
		Player bot = new AIPlayer(botname, Mark.YELLOW, type);
		
		ArrayList<Player> players = new ArrayList<>();
		players.add(new RemotePlayer(this, Mark.RED));
		players.add(bot);
		server.startGame(4, players);
	}
	
	
	/**
	 * leaves a game if the current SessionState is ingame, sets the
	 * SessionState to lobby if successful.
	 */
	// @ requires getState() == SessionState.ingame;
	// @ ensures getPlayer() == null;
	// @ ensures getState() == SessionState.lobby;
	private void leaveGame() {
		if (state != SessionState.ingame) {
			showModalMessage("You need to be in a game to leave one.");
			return;
		}
		setState(SessionState.lobby);
		server.broadcastPlayers();
		player.getGame().deleteObserver(player);
		setPlayer(null);
		// TODO tell our opponent about it, can't with protocol
	}
	
	/**
	 * Executes a move, next turn if successful.
	 * 
	 * @param row
	 *            chosen row
	 * @param col
	 *            chosen col
	 */
	// @ requires getState() == SessionState.ingame;
	// @ requires row >= 0 && col >= 0 && row < size && col < size;
	private void commitMove(int row, int col) {
		if (state != SessionState.ingame) {
			sendString("error invalidMove");
			return;
		}
		Game game = player.getGame();
		int size = game.getBoard().getSize();
		if (row < 0 || row >= size || col < 0 || col >= size) {
			sendString("error invalidMove");
			return;
		}
		game.commitMove(player, col, row);
	}
	
	/**
	 * Sets the SessionState to queued if the current SessionState is lobby.
	 * Calls server.findQueue() if successful.
	 */
	// @ requires getState() == SessionState.lobby;
	public void queueGame() {
		if (state != SessionState.lobby) {
			showModalMessage("You need to be in the lobby to queue");
			return;
		}
		state = SessionState.queued;
		sendString(Protocol.WAITING);
		server.findQueue();
	}
	
	/**
	 * Sets the SessionState to lobby if the current SessionState is queued.
	 */
	// @ requires getState() == SessionState.queued;
	// @ ensures getState() == SessionState.lobby;
	public void unQueueGame() {
		if (state != SessionState.queued) {
			showModalMessage("You need to be queued in order to leave the queue");
			return;
		}
		state = SessionState.lobby;
		sendString(Protocol.LOBBY);
		server.broadcastPlayers();
	}
	
	/**
	 * Attempts to log in with the given name, will send errors if the name is
	 * not available or valid or if this connection can not log in at this
	 * moment
	 * 
	 * @param name
	 *            chosen name
	 */
	// @ requires getState() == SessionState.authenticating;
	// @ requires name != null;
	// @ requires !name.matches("^\\w+$");
	// @ ensures getState() == SessionState.lobby;
	// @ ensures getName() != "";
	private void login(String name) {
		if (state != SessionState.authenticating) {
			showModalMessage("Already logged in. You can't set your name at this time");
			return;
		}
		
		if (!name.matches("^\\w+$")) {
			sendString("error invalidCharacters");
			return;
		}
		if (server.findPlayer(name) != null) {
			sendString("error nameTaken");
			return;
		}
		
		this.name = name;
		setState(SessionState.lobby);
		sendString(Protocol.LOBBY);
		
		server.broadcastPlayers();
	}
	
	/**
	 * disconnect by closing the this.sock
	 */
	public void disconnect() {
		sock.close();
		if (state == SessionState.ingame) {
			player.getGame().deleteObserver(player);
			// TODO stop the game if this player is a player of the game
		}
		
		setState(SessionState.disconnected);
		if (isLoggedIn()) {
			server.broadcastPlayers();
		}
	}
	
	/**
	 * getter name
	 * 
	 * @return this.name
	 */
	/* @ pure */ public String getName() {
		return name;
	}
	
	/**
	 * getter state
	 * 
	 * @return this.state
	 */
	/* @ pure */ public SessionState getState() {
		return state;
	}
	
	/**
	 * setter this.state
	 * 
	 * @param state
	 */
	// @ ensures getState() == state;
	public void setState(SessionState state) {
		this.state = state;
	}
	
	/**
	 * Send an error message
	 * 
	 * @param message
	 *            error message to send.
	 */
	/* @ pure */ public void showModalMessage(String message) {
		sendString("error errorMessage " + message);
	}
	
	/**
	 * Send a String str trough the AsyncSocket this.sock
	 * 
	 * @param str
	 *            string to send trough this.sock
	 */
	/* @ pure */ public void sendString(String str) {
		System.out.println(name + "\t<< " + str);
		sock.sendPacket(socketProtocol.textPacket(str));
	}
	
	/**
	 * setter server Player
	 * 
	 * @param player
	 *            server Player object
	 */
	// @ ensures getPlayer() == player;
	public void setPlayer(RemotePlayer player) {
		this.player = player;
	}
	
	/**
	 * getter server Player
	 * 
	 * @return this.player
	 */
	/* @ pure */ public RemotePlayer getPlayer() {
		return player;
	}
	
	/**
	 * getter Server
	 * 
	 * @return this.server
	 */
	/* @ pure */ public Server getServer() {
		return server;
	}
	
	/**
	 * Gets whether this client connection is a player that is logged in
	 * 
	 * @return
	 */
	/* @ pure */ public boolean isLoggedIn() {
		return state == SessionState.lobby || state == SessionState.queued
				|| state == SessionState.ingame;
	}
}


























