package http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	
	private static HashMap<String, HttpPage> pages = new HashMap<>();
	private static String mimeText = "text/html; charset=utf-8";
	private static String mimePng = "image/png";
	
	static {
		try {
			addPage("/",
					new HttpPage(mimeText, Files.readAllBytes(Paths.get("resources/index.html"))));
			addPage("/scripts.js",
					new HttpPage(mimeText, Files.readAllBytes(Paths.get("resources/scripts.js"))));
			addPage("/util.js",
					new HttpPage(mimeText, Files.readAllBytes(Paths.get("resources/util.js"))));
			addPage("/icon.png",
					new HttpPage(mimePng, Files.readAllBytes(Paths.get("resources/icon.png"))));
		} catch (IOException e) {
			System.out.println("Failed to load http files");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Used to test if a tcp connection is attempting to use the http protocol.
	 * 
	 * @param packet
	 * @return returns true if the packet was a http request
	 */
	public static boolean tryRespondGET(byte[] packet, AsyncSocket con) {
		String packetText = new String(packet, StandardCharsets.US_ASCII);
		String header = packetText.split("\r\n\r\n")[0];
		
		String[] headers = header.split("\r\n");
		Pattern reg = Pattern.compile("^GET ([^ ]+) HTTP/1\\.1$");
		Matcher m = reg.matcher(headers[0]);
		if (!m.find()) {
			return false;
		}
		String path = m.group(1);
		
		System.out.println("HTTP GET request: " + path);
		if (pages.containsKey(path)) {
			HttpPage page = pages.get(path);
			String headerstr = "";
			headerstr += "HTTP/1.1 200 OK\r\n";
			// TODO support other mime types
			headerstr += "Content-Type: " + page.getMime() + "\r\n";
			byte[] response = getResponseBytes(headerstr, page.getBytes());
			con.sendPacket(response);
		} else {
			String headerstr = "";
			headerstr += "HTTP/1.1 404 Not Found\r\n";
			headerstr += "Content-Type: " + mimeText + "\r\n";
			byte[] response = getResponseBytes(headerstr,
					"<h1>404 - Not Found</h1>".getBytes(StandardCharsets.UTF_8));
			con.sendPacket(response);
		}
		return true;
	}
	
	/**
	 * Add the content-length header and combines it with the message body and
	 * returns the byte array to send.
	 * 
	 * @param headerstr
	 *            the header string wihout content-length header
	 * @param pagestr
	 *            the body of the message
	 * @return the bytes of the response
	 */
	public static byte[] getResponseBytes(String headerstr, byte[] body) {
		String returnHeaderString = headerstr;
		returnHeaderString += "Content-Length: " + body.length + "\r\n";
		returnHeaderString += "\r\n";
		byte[] header = returnHeaderString.getBytes(StandardCharsets.US_ASCII);
		byte[] response = new byte[header.length + body.length];
		System.arraycopy(header, 0, response, 0, header.length);
		System.arraycopy(body, 0, response, header.length, body.length);
		return response;
	}
	
	/**
	 * adds a page to the server.
	 * 
	 * @param path
	 *            the path and filename of the page relative to the server root,
	 *            eg: /index.html
	 * @param page
	 *            the string to serve
	 */
	public static void addPage(String path, HttpPage page) {
		pages.put(path, page);
	}
	
	public static class HttpPage {
		private String mime;
		private byte[] bytes;
		
		public HttpPage(String mime, byte[] bytes) {
			this.mime = mime;
			this.bytes = bytes;
		}
		
		public byte[] getBytes() {
			return bytes;
		}
		
		public String getMime() {
			return mime;
		}
	}
}


















