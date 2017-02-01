package http;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

import common.AsyncSocket;
import common.SocketProtocol;

/**
 * A protocol message parser for the websocket protocol. This class deals with
 * all packets received from a websocket and can be used to build packets to
 * send to the websocket.
 * 
 * @author Wilbert
 *
 */
public class WebSocketProtocol implements SocketProtocol {
	private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final int OPTCODE_CLOSE = 0x8;
	private static final int OPTCODE_PING = 0x9;
	private static final int OPTCODE_PONG = 0xA;
	private static final int OPTCODE_TEXT = 0x1;
	private static final int OPTCODE_BINARY = 0x2;
	
	private AsyncSocket sock;
	
	@Override
	public void setSocket(AsyncSocket asock) {
		this.sock = asock;
	}
	
	@Override
	public byte[] textPacket(String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		return craftDataPacket(bytes, OPTCODE_TEXT);
	}
	
	public byte[] craftDataPacket(byte[] payload, int optcode) {
		if (payload.length >= 1 << 16) {
			throw new RuntimeException("Packet to large to send");
		}
		byte[] packet = new byte[2 + (payload.length > 125 ? 2 : 0) + payload.length];
		int i = 0;
		// write packet the flags
		packet[i] |= 0b10000000; // fin
		packet[i] |= optcode; // optcode
		i++;
		// write the length
		packet[i] |= payload.length > 125 ? 126 : payload.length; // tinylength
		i++;
		if (payload.length > 125) {
			packet[i++] = (byte) (payload.length >> 8);
			packet[i++] = (byte) (payload.length & 0xff);
		}
		// copy the payload
		System.arraycopy(payload, 0, packet, i, payload.length);
		return packet;
	}
	
	@Override
	public String parsePacket(byte[] packet) {
		try {
			// header of the packet
			int i = 0;
			boolean fin = (packet[i] & 0b10000000) != 0;
			int optcode = packet[i] & 0b00001111;
			i++;
			boolean mask = (packet[i] & 0b10000000) != 0;
			int length = packet[i] & 0b01111111;
			i++;
			if (length == 126) {
				length = packet[i++] << 8 + packet[i++];
			} else if (length == 127) {
				sock.connectionClosed(); // 65kb packets not supported
			}
			byte[] maskKey = new byte[4];
			if (mask) {
				maskKey[0] = packet[i++];
				maskKey[1] = packet[i++];
				maskKey[2] = packet[i++];
				maskKey[3] = packet[i++];
			}
			
			// payload part of packet
			byte[] payload = new byte[length];
			System.arraycopy(packet, i, payload, 0, length);
			if (mask) {
				for (int a = 0; a < length; a++) {
					payload[a] ^= maskKey[a % 4];
				}
			}
			
			if (!fin) {
				sock.connectionClosed(); // multipacket messages not suported
			}
			// close optcode
			switch (optcode) {
				case OPTCODE_CLOSE:
					sock.connectionClosed();
					break;
				case OPTCODE_PING:
					sock.sendPacket(craftDataPacket(payload, OPTCODE_PONG));
					break;
				case OPTCODE_PONG:
					break;
				case OPTCODE_TEXT:
					return new String(payload, StandardCharsets.UTF_8);
				case OPTCODE_BINARY:
					sock.connectionClosed(); // not supported
					break;
			}
		} catch (IndexOutOfBoundsException ex) {
			sock.connectionClosed();
		}
		return null;
	}
	
	/**
	 * Calculates the key header for a new websocket connection
	 * 
	 * @param clientkey
	 *            the key given by the client
	 * @return the appropriate response key
	 */
	public static String calculateWebsocketKey(String clientkey) {
		// ok this is a bit silly but it's actually part of the http protocol
		String added = clientkey + WEBSOCKET_GUID;
		byte[] sha1 = DigestUtils.sha1(added);
		return Base64.getEncoder().encodeToString(sha1);
	}
	
	/**
	 * Examines a packet an decides if it is a http request for a websocket. If
	 * it is, the handshake is completed and a WebsocketProtocol object is
	 * returned that can be used to communicate through this websocket
	 *
	 * @param packet
	 *            the binary packet
	 * @param con
	 *            the socket
	 * @return returns a WebsocketProtocol is successful or null otherwise
	 */
	public static WebSocketProtocol tryConnectWebsocket(byte[] packet, AsyncSocket con) {
		String packetText = new String(packet, StandardCharsets.US_ASCII);
		String header = packetText.split("\r\n\r\n")[0];
		
		// check if we have the upgrade header
		Pattern upreg = Pattern.compile("^Upgrade:\\s+websocket\\s*$", Pattern.MULTILINE);
		Matcher upmatcher = upreg.matcher(header);
		if (!upmatcher.find()) {
			return null;
		}
		
		// find the Sec-WebSocket-Key header
		Pattern keyreg = Pattern.compile("^Sec-WebSocket-Key:\\s+([\\w=+\\/]+)\\s*$",
				Pattern.MULTILINE);
		Matcher keymatcher = keyreg.matcher(header);
		if (!keymatcher.find()) {
			return null;
		}
		String clientkey = keymatcher.group(1);
		String serverkey = calculateWebsocketKey(clientkey);
		
		// we found a valid upgrade request, send a response
		String headerstr = "";
		headerstr += "HTTP/1.1 101 Switching Protocols\r\n";
		headerstr += "Upgrade: websocket\r\n";
		headerstr += "Connection: Upgrade\r\n";
		headerstr += "Sec-WebSocket-Accept: " + serverkey + "\r\n";
		
		con.sendPacket(BasicHttpServer.getResponseBytes(headerstr, new byte[0]));
		
		WebSocketProtocol protocol = new WebSocketProtocol();
		protocol.setSocket(con);
		return protocol;
	}
}










