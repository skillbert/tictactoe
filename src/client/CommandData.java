package client;

import common.SessionState;

public class CommandData {
    public final int minArgs;
    public final String usage;
    public final SessionState requiredState;
    public final String wrongStateMessage;
    public final String handler;
    
    public CommandData(int minArgs, String usage, SessionState requiredState, String wrongStateMessage, String handler) {
        this.minArgs = minArgs;
        this.usage = usage;
        this.requiredState = requiredState;
        this.wrongStateMessage = wrongStateMessage;
        this.handler = handler;
    }
}
