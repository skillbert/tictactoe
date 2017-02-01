package client;

public class Client {
	private static Session session;
	
	/**
	 * Starts the client by creating a Session and executing run() on it.
	 * 
	 * @param args
	 *            currently unused args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		session = new Session();
		session.run();
	}
	
}
















