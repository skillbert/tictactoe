package common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Protocol {
	public static final int DEFAULTPORT = 12345;
	public static final Charset charset = StandardCharsets.UTF_16;

	/**
	 * If the server should follow the exact protocol, hopefully the protocol
	 * will get to a state where this can be removed
	 */
	public static boolean followStandards = false;
}
