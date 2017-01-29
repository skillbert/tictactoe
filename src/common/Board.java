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
	public static final int INVALID_INDEX = -1;
	public final int DIM = 4;
	public final int winLength = DIM;
	private ArrayList<int[]> wincons;
	private int[] fields;
	
	
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
	public Board() {
		fields = new int[DIM * DIM * DIM];
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
		Board board = new Board();
		IntStream.range(0, DIM * DIM * DIM).forEach(i -> board.setField(i, this.getField(i)));
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
		int wl = DIM;// win length
		for (int[] step : stepdirs) {
			for (int row = 0; row < DIM; row++) {
				if (row + step[0] * (wl - 1) < 0 || row + step[0] * (wl - 1) >= DIM) {
					continue;
				}
				for (int col = 0; col < DIM; col++) {
					if (col + step[1] * (wl - 1) < 0 || col + step[1] * (wl - 1) >= DIM) {
						continue;
					}
					for (int height = 0; height < DIM; height++) {
						if (height + step[2] * (wl - 1) < 0 || height + step[2] * (wl - 1) >= DIM) {
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
		return row + (column * DIM) + (height * DIM * DIM);
	}
	
	/**
	 * Gets the x,y position of a field with the given index
	 * 
	 * @param index
	 * @return
	 */
	public Point position(int index) {
		return new Point((index / DIM) % DIM, index % DIM);
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
		for (int y = 0; y < DIM; y++) {
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
		return 0 <= index && index < DIM * DIM * DIM;
	}
	
	public boolean isField(int row, int column, int height) {
		return row >= 0 && row < DIM && column >= 0 && column < DIM && height >= 0 && height < DIM;
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
				&& (!isField(index - DIM * DIM) || getField(index - DIM * DIM) != Mark.EMPTY);
	}
	
	/**
	 * @return true if the board is full
	 */
	public boolean isFull() {
		return IntStream.range(0, DIM * DIM * DIM).allMatch(i -> getField(i) != Mark.EMPTY);
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
		return DIM;
	}
	
	public ArrayList<int[]> getWinConditions() {
		return wincons;
	}
	
	public int getWinLength() {
		return DIM;
	}
	
	public int[] getFieldsClone() {
		return fields.clone();
	}
	
	@Override
	public String toString() {
		String str = "      1      2      3      4";
		for (int i = 0; i < DIM; i++) {
			str += "\n   " + new String(new char[DIM]).replace("\0", "+------") + "+ \n ";
			str += String.valueOf(i + 1) + " ";
			for (int j = 0; j < DIM; j++) {
				str += "| ";
				for (int k = 0; k < DIM; k++) {
					str += Mark.getMarkString(getField(index(i, j, k)));
				}
				str += " ";
			}
			str += "| ";
		}
		str += "\n   " + new String(new char[DIM]).replace("\0", "+------") + "+ \n";
		return str;
	}
	
	public int getFieldLength() {
		return DIM * DIM * DIM;
	}
}
