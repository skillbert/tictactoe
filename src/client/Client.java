package client;

import java.util.Observable;

public class Client extends Observable {
	private static Session session;

	public static void main(String[] args) throws Exception {
		session = new Session();
		session.run();
	}

}


















