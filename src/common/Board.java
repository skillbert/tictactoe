package common;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Board for the Connect3D game. Module 2 Programming project.
 * 
 * @author Maurits van der Vijgh
 * @version $0.1$
 */
public class Board {
	public static final int DEFAULTSIZE = 4;
	public static final int INVALID_INDEX = -1;
	private ArrayList<int[]> wincons;
	private int[] fields;
	private int size;
	private int winLength;
	
	
	/**
	 * fields represent the board. column/row to index.
	 * 
	 * Sorry the code formatter really doesn't like this table
	 * 
	 * ____c1__c2__c3__c4
	 * 
	 * r1__0___4___8___12
	 * 
	 * r2__1___5___9___13
	 * 
	 * r3__2___6___10__14
	 * 
	 * r4__3___7___11__15
	 * 
	 * DIM of these 2D grids are stacked on top of each other to achieve a 3D
	 * board to get the index of a certain row and column combination you have
	 * to add 16 for each layer on top of the base one.
	 */
	public Board(int boardSize) {
		this.size = boardSize;
		this.winLength = boardSize;
		fields = new int[size * size * size];
		Arrays.fill(fields, Mark.EMPTY);
		
		initializeWincons();
	}
	
	/**
	 * Returns a new Board object with the same fields marked as the current
	 * board
	 * 
	 * @return deepcopy of the current Board object
	 */
	public Board deepCopy() {
		Board board = new Board(size);
		IntStream.range(0, size * size * size).forEach(i -> board.setField(i, this.getField(i)));
		return board;
	}
	
	/**
	 * Calculates all win conditions for this board.
	 */
	public void initializeWincons() {
		// loop all possible directions
		ArrayList<int[]> stepdirs = new ArrayList<>();
		for (int rowstep = -1; rowstep <= 1; rowstep++) {
			for (int colstep = -1; colstep <= 1; colstep++) {
				for (int heightstep = -1; heightstep <= 1; heightstep++) {
					// ignore case where all are 0
					if (rowstep == 0 && colstep == 0 && heightstep == 0) {
						continue;
					}
					int[] step = new int[] {rowstep, colstep, heightstep };
					// don't add existing but mirrored steps
					if (stepdirs.stream().anyMatch(
							s -> s[0] == -step[0] && s[1] == -step[1] && s[2] == -step[2])) {
						continue;
					}
					stepdirs.add(step);
				}
			}
		}
		// calculate all win conditions
		wincons = new ArrayList<>();
		int wl = winLength;// win length
		for (int[] step : stepdirs) {
			for (int row = 0; row < size; row++) {
				if (row + step[0] * (wl - 1) < 0 || row + step[0] * (wl - 1) >= size) {
					continue;
				}
				for (int col = 0; col < size; col++) {
					if (col + step[1] * (wl - 1) < 0 || col + step[1] * (wl - 1) >= size) {
						continue;
					}
					for (int height = 0; height < size; height++) {
						if (height + step[2] * (wl - 1) < 0
								|| height + step[2] * (wl - 1) >= size) {
							continue;
						}
						int[] wincon = new int[wl];
						for (int i = 0; i < wl; i++) {
							wincon[i] = index(row + i * step[0], col + i * step[1],
									height + i * step[2]);
						}
						wincons.add(wincon);
					}
				}
			}
		}
		
		// wincons.stream().forEach(w -> System.out.println(w[0] + " " + w[1] +
		// " " + w[2] + " " + w[3]));
	}
	
	/**
	 * Return the index of the field at row, column, height.
	 * 
	 * @param row
	 * @param column
	 * @param height
	 * @return the selected field
	 */
	public int index(int row, int column, int height) {
		return row + (column * size) + (height * size * size);
	}
	
	/**
	 * Gets the x,y position of a field with the given index
	 * 
	 * @param index
	 * @return
	 */
	public Point position(int index) {
		return new Point((index / size) % size, index % size);
	}
	
	/**
	 * Returns the index where a piece will fall if inserted at the given row
	 * and column, returns INVALID_INDEX in case the column is full
	 * 
	 * @param row
	 * @param column
	 * @return the index of the field or INVALID_INDEX if the column is full
	 */
	public int indexFromColumn(int row, int column) {
		for (int y = 0; y < size; y++) {
			int i = index(row, column, y);
			if (fields[i] == Mark.EMPTY) {
				return i;
			}
		}
		return INVALID_INDEX;
	}
	
	/**
	 * Returns true if the field index is within range.
	 * 
	 * @param index
	 * @return true if the field exists
	 */
	public boolean isField(int index) {
		return 0 <= index && index < size * size * size;
	}
	
	public boolean isField(int row, int column, int height) {
		return row >= 0 && row < size && column >= 0 && column < size && height >= 0
				&& height < size;
	}
	
	/**
	 * Returns Mark at index in fields
	 * 
	 * @param index
	 * @return Mark
	 */
	public int getField(int index) {
		return fields[index];
	}
	
	/**
	 * Sets index index in fields to Mark m
	 * 
	 * @param index
	 * @param m
	 */
	public void setField(int index, int m) {
		fields[index] = m;
	}
	
	/**
	 * Returns true if the field below the chosen field does not exist or is
	 * filled.
	 * 
	 * @param index
	 * @return true if the field is available
	 */
	public boolean isAvailableField(int index) {
		return getField(index) == Mark.EMPTY
				&& (!isField(index - size * size) || getField(index - size * size) != Mark.EMPTY);
	}
	
	/**
	 * @return true if the board is full
	 */
	public boolean isFull() {
		return IntStream.range(0, size * size * size).allMatch(i -> getField(i) != Mark.EMPTY);
	}
	
	/**
	 * Determines if the board has a winner and returns the winner
	 * 
	 * @return the winning mark or Mark.Empty if there is no winner
	 */
	public int findWinner() {
		for (int[] wincon : wincons) {
			int mark = fields[wincon[0]];
			if (mark == Mark.EMPTY) {
				continue;
			}
			boolean winner = true;
			for (int i : wincon) {
				if (fields[i] != mark) {
					winner = false;
				}
			}
			if (winner) {
				return mark;
			}
		}
		return Mark.EMPTY;
	}
	
	/**
	 * Resets the board
	 */
	public void reset() {
		Arrays.fill(fields, Mark.EMPTY);
	}
	
	public int getSize() {
		return size;
	}
	
	public ArrayList<int[]> getWinConditions() {
		return wincons;
	}
	
	public int getWinLength() {
		return winLength;
	}
	
	public int[] getFieldsClone() {
		return fields.clone();
	}
	
	@Override
	public String toString() {
		String str = "     ";
		// column numbers
		for (int i = 0; i < size; i++) {
			str += (i + 1) + Util.repeatString(" ", size + 2);
		}
		// draw the field
		for (int i = 0; i < size; i++) {
			str += "\n   " + Util.repeatString("+" + Util.repeatString("-", size + 2), size)
					+ "+ \n";
			str += " " + (i + 1) + " ";
			for (int j = 0; j < size; j++) {
				str += "| ";
				for (int k = 0; k < size; k++) {
					str += Mark.getMarkString(getField(index(i, j, k)));
				}
				str += " ";
			}
			str += "| ";
		}
		str += "\n   " + Util.repeatString("+" + Util.repeatString("-", size + 2), size) + "+ \n";
		return str;
	}
	
	public int getFieldLength() {
		return size * size * size;
	}
}
