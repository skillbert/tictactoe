package common;

/**
 * A protocol wrapper to parse binary messages using the standard protcol
 * 
 * @author Wilbert
 *
 */
public class DirectProtocol implements SocketProtocol {
	@Override
	public void setSocket(AsyncSocket sock) {
	}

	@Override
	public byte[] textPacket(String str) {
		return (str + "\n").getBytes(Protocol.charset);
	}

	@Override
	public String parsePacket(byte[] bytes) {
		// TODO might have to collect and stall these packets until we find a \n
		// in case another group writes partial messages to their socket
		String message = new String(bytes, Protocol.charset);
		return message.split("\\s+$")[0];
	}
}
