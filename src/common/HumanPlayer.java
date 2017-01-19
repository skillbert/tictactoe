package common;

import java.util.Scanner;


/**
 * @deprecated going to have to rebuild this with network in mind
 */
@Deprecated
public class HumanPlayer implements Player {
	private String name;
	private Mark mark;

	public HumanPlayer(String name, Mark mark) {
		this.name = name;
		this.mark = mark;
	}

	@Override
	public int determineMove(Board board) {
		String prompt = "> " + getName() + " (" + getMark().toString() + ")" + ", what is your choice? ";
		int choice = readInt(prompt);
		boolean valid = board.isField(choice) && board.isAvailableField(choice);
		while (!valid) {
			System.out.println("ERROR: field " + choice + " is no valid choice.");
			choice = readInt(prompt);
			valid = board.isField(choice) && board.isAvailableField(choice);
		}
		return choice;
	}

	private int readInt(String prompt) {
		int value = 0;
		boolean intRead = false;
		@SuppressWarnings("resource")
		Scanner line = new Scanner(System.in);
		do {
			System.out.print(prompt);
			try (Scanner scannerLine = new Scanner(line.nextLine())) {
				if (scannerLine.hasNextInt()) {
					intRead = true;
					value = scannerLine.nextInt();
				}
			}
		} while (!intRead);
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Mark getMark() {
		return mark;
	}

	@Override
	public void makeMove(Board board) {
		// TODO Auto-generated method stub

	}

}
