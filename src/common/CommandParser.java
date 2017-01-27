package common;

/**
 * Simple class to parse commands. A command is a whitespace seperated list of
 * substrings, the first one being the command and all following strings are the
 * arguments
 * 
 * @author Wilbert
 *
 */
public class CommandParser {
	private String[] parts;
	private int index;
	private String original;

	/**
	 * Creates a new command from the given string
	 * 
	 * @param command
	 *            the command string to parse
	 */
	public CommandParser(String command) {
		index = 1;
		original = command;
		parts = command.split("\\s+");
	}

	/**
	 * gets the amount of arguments, that is the amount of arguments strings
	 * excluding the command itself
	 * 
	 * @return
	 */
	public int getArgCount() {
		return parts.length - 1;
	}

	/**
	 * Gets the command.
	 * 
	 * @return
	 */
	public String getCommand() {
		return parts[0];
	}

	/**
	 * Gets the next argument and returns it as a string.
	 * 
	 * @return the next argument
	 * @throws CommandFormatException
	 *             Is thrown when there are no arguments left in the command
	 */
	public String nextString() throws CommandFormatException {
		if (!hasNext()) {
			throw new CommandFormatException();
		}
		return parts[index++];
	}

	/**
	 * Gets the next argument or returns the fallback value is there are no
	 * arguments left
	 * 
	 * @param fallback
	 *            The string to return if there are no arguments left
	 * @return The next argument or the fallback string
	 */
	public String nextStringOpt(String fallback) {
		if (!hasNext()) {
			return fallback;
		}
		return parts[index++];
	}

	/**
	 * Returns the remaining string from the last argument on
	 * 
	 * @return
	 */
	public String remainingString() {
		if (!hasNext()) {
			return "";
		}
		return original.split("\\s+", index + 1)[index];
	}


	/**
	 * Parses the next argument as an int and returns it
	 * 
	 * @return The next argument parsed as int
	 * @throws CommandFormatException
	 *             Thrown if there is no next argument or if the argument can't
	 *             be parsed as int
	 */
	public int nextInt() throws CommandFormatException {
		return nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Parses the next argument as an in in the given range
	 * 
	 * @param min
	 *            the minimum value (inclusive)
	 * @param max
	 *            the maximum value (inclusive)
	 * @return returns the parsed int on success
	 * @throws CommandFormatException
	 *             if there is no next argument or if it can't be parsed as an
	 *             int in the given range
	 */
	public int nextInt(int min, int max) throws CommandFormatException {
		if (!hasNext()) {
			throw new CommandFormatException();
		}
		try {
			int value = Integer.parseInt(parts[index++]);
			if (value > max || value < min) {
				throw new CommandFormatException();
			}
			return value;
		} catch (NumberFormatException ex) {
			throw new CommandFormatException();
		}
	}

	/**
	 * Checks if there is a next argument
	 * 
	 * @return true if there is a next argument, false if there are no more
	 *         arguments
	 */
	public boolean hasNext() {
		return index < parts.length;
	}

	public static class CommandFormatException extends Exception {
		public CommandFormatException(String message) {
			super(message);
		}

		public CommandFormatException() {
			super();
		}
	}
}




























