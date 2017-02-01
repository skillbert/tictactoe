package client.command;

import java.util.HashMap;
import java.util.Map;

import client.Session;
import common.SessionState;

public final class Commands {
	public final Map<String, CommandHandler> commands = new HashMap<String, CommandHandler>();
	
	public Commands(Session session) {
		commands.put("connect", new ConnectHandler(session, 2, "<host> [port]",
				SessionState.disconnected, "Already connected"));
		commands.put("login", new LoginHandler(session, 2, "<name>", SessionState.authenticating,
				"Not connected or already logged in"));
		commands.put("queue", new QueueHandler(session, 1, "", SessionState.lobby,
				"Already queued or not in the lobby"));
		commands.put("cancel_queue",
				new CancelQueueHandler(session, 1, "", SessionState.queued, "Not queued"));
		commands.put("test",
				new TestHandler(session, 1, "", SessionState.disconnected, "Already connected"));
		commands.put("place",
				new PlaceHandler(session, 3, "<x> <y>", SessionState.ingame, "Not in a game"));
		commands.put("invite", new InviteHandler(session, 1, "<boardsize> [player1] ...",
				SessionState.lobby, "You need to be in the lobby to invite others"));
		commands.put("reply", new ReplyHandler(session, 2, "(yes|no)", SessionState.invited,
				"You don't have any invitations to reply to"));
		commands.put("suggestmove",
				new SuggestMoveHandler(session, 3, "(easy|medium|hard) <thinktime>",
						SessionState.ingame, "You need to be in-game to get a move suggestion"));
		commands.put("bot", new BotGameHandler(session, 2, "(easy|medium|hard)", SessionState.lobby,
				"You need to be in the lobby to start a new game."));
	}
}
