package httpServer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.AsyncSocket;

/**
 * Very basic implementation of the http protocol. This implementation is
 * compatible with the http protocol but does not enforce it
 * 
 * @author Wilbert
 *
 */
public class BasicHttpServer {

	private static HashMap<String, String> pages = new HashMap<>();


	static {
		addPage("/", "omfg it works");
	}


	/**
	 * Used to test if a tcp connection is attempting to use the http protocol
	 * 
	 * @param packet
	 * @return returns true if the packet was a http request
	 */
	public static boolean tryRespondGET(String packet, AsyncSocket con) {
		String header = packet.split("\r\n\r\n")[0];

		String[] headers = header.split("\r\n");
		Pattern reg = Pattern.compile("^GET ([^ ]+) HTTP/1\\.1$");
		Matcher m = reg.matcher(headers[0]);
		if (!m.find()) {
			return false;
		}
		String path = m.group(1);

		System.out.println("HTTP GET request: " + path);
		if (pages.containsKey(path)) {
			String pagestr = pages.get(path);
			String headerstr = "";
			headerstr += "HTTP/1.1 200 OK\r\n";
			// TODO support other mime types
			headerstr += "Content-Type: text/html; charset=utf-8\r\n";
			byte[] response = getResponseBytes(headerstr, pagestr);
			con.sendPacket(response);
		} else {
			String headerstr = "";
			headerstr += "HTTP/1.1 404 Not Found\r\n";
			byte[] response = getResponseBytes(headerstr, "<h1>404 - Not Found</h1>");
			con.sendPacket(response);
		}
		return true;
	}

	/**
	 * Add the content-length header and combines it with the message body and
	 * returns the byte array to send
	 * 
	 * @param headerstr
	 *            the header string wihout content-length header
	 * @param pagestr
	 *            the body of the message
	 * @return the bytes of the response
	 */
	public static byte[] getResponseBytes(String headerstr, String pagestr) {
		byte[] body = pagestr.getBytes(StandardCharsets.UTF_8);
		headerstr += "Content-Length: " + body.length + "\r\n";
		headerstr += "\r\n";
		byte[] header = headerstr.getBytes(StandardCharsets.US_ASCII);
		byte[] response = new byte[header.length + body.length];
		System.arraycopy(header, 0, response, 0, header.length);
		System.arraycopy(body, 0, response, header.length, body.length);
		return response;
	}

	/**
	 * adds a page to the server
	 * 
	 * @param path
	 *            the path and filename of the page relative to the server root,
	 *            eg: /index.html
	 * @param page
	 *            the string to serve
	 */
	public static void addPage(String path, String page) {
		pages.put(path, page);
	}
}


















