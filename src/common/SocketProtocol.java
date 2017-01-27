package common;

/**
 * An interface to deal with the parsing of different socket level protocols
 * 
 * @author Wilbert
 *
 */
public interface SocketProtocol {

	/**
	 * Sets the underlaying AsyncSocket object. The implementing class can use
	 * this socket to automatically respond to standard packet
	 * 
	 * @param sock
	 *            The AsyncSocket that is using this SocketProtocol object
	 */
	public void setSocket(AsyncSocket sock);

	/**
	 * Builds a packet containing a raw text message
	 * 
	 * @param str
	 *            The string to encode in the packet
	 * @return a byte[] that can be sent through the socket and contains the
	 *         string
	 */
	public byte[] textPacket(String str);

	/**
	 * Parses a received packet and returns a String if the packet is a data
	 * packet containing a string. This function automatically responds to
	 * control packets
	 * 
	 * @param bytes
	 *            The packet to parse
	 * @return Returns a String if the packet is a data packet, or null
	 *         otherwise
	 */
	public String parsePacket(byte[] bytes);
}
