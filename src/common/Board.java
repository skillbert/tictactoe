package common;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Board for the Connect3D game. Module 2 Programming project.
 * 
 * @author Maurits van der Vijgh
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
	
	// @ ensures (\forall int i; 0 <= i & i < getFields().length; getFields()[i]
	// == Mark.EMPTY);
	// @ ensures getWinConditions().size() > 0;
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
	
	// @ ensures (\forall int j; 0<=j & j < getFields().length; getFields()[j]
	// == \result.getField(j));
	/* @ pure */ public Board deepCopy() {
		Board board = new Board(size);
		IntStream.range(0, size * size * size).forEach(i -> board.setField(i, this.getField(i)));
		return board;
	}
	
	/**
	 * Calculates all win conditions for this board.
	 */
	// @ ensures (\forall int i; 0 <= i & i < getWinConditions().size();
	// getWinConditions().get(i).length == winLength);
	// @ ensures getWinConditions().size() > 0;
	public void initializeWincons() {
		// loop all possible directions
		ArrayList<int[]> stepdirs = new ArrayList<int[]>();
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
		wincons = new ArrayList<int[]>();
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
	}
	
	/**
	 * Return the index of the field at row, column, height.
	 * 
	 * @param row
	 * @param column
	 * @param height
	 * @return the selected field
	 */
	
	// @ requires height < DIM & height > 0 & column < DIM & column > DIM & row
	// < DIM & row > DIM;
	// @ ensures \result < getFieldLength();
	/* @ pure */ public int index(int row, int column, int height) {
		return row + (column * size) + (height * size * size);
	}
	
	/**
	 * Gets the x,y position of a field with the given index
	 * 
	 * @param index
	 * @return
	 */
	// @ requires index < getFieldLength();
	// @ ensures \result.x < DIM & \result.y < DIM & \result.x > 0 & \result.y >
	// 0;
	/* @ pure */ public Point position(int index) {
		return new Point((index / size) % size, index % size);
	}
	
	/**
	 * Returns the index where a piece will fall if inserted at the given row
	 * and column, returns INVALID_INDEX in case the column is full
	 * 
	 * @param row
	 * @param column
	 * @return the index of the field or INVALID_INDEX if the column is full //@
	 *         requires row < DIM & column < DIM & row > DIM & row > DIM; //@
	 *         ensures \result == INVALID_INDEX || getField(\result) ==
	 *         Mark.EMPTY; /*@ pure
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
	
	// @ ensures \result & index < getFieldLength() & index >= 0 || !\result &
	// (index > getFieldLength() || index < 0);
	/* @ pure */ public boolean isField(int index) {
		return 0 <= index && index < size * size * size;
	}
	
	/**
	 * Returns true if the selected field is within range.
	 * 
	 * @param index
	 * @return true if the field exists
	 */
	// @ ensures \result == isField(index(row, column, height));
	/* @ pure */ public boolean isField(int row, int column, int height) {
		return row >= 0 && row < size && column >= 0 && column < size && height >= 0
				&& height < size;
	}
	
	/**
	 * Returns Mark at index in fields
	 * 
	 * @param index
	 * @return Mark
	 */
	// @ requires isField(index);
	// @ ensures \result == getFields()[index];
	/* @ pure */ public int getField(int index) {
		return fields[index];
	}
	
	/**
	 * Sets index index in fields to Mark m
	 * 
	 * @param index
	 * @param m
	 */
	// @ requires isField(index) & m >= Mark.EMPTY & m <= Mark.YELLOW;
	// @ ensures getField(index) == m;
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
	/*
	 * @ ensures ((\result & isField(getField(index)) & getField(index) ==
	 * Mark.EMPTY || getField(index - DIM * DIM) != Mark.EMPTY) || (!\result &
	 * !(isField(getField(index)) & getField(index) == Mark.EMPTY))); pure
	 */ public boolean isAvailableField(int index) {
		return getField(index) == Mark.EMPTY
				&& (!isField(index - size * size) || getField(index - size * size) != Mark.EMPTY);
	}
	
	/**
	 * @return true if the board is full
	 */
	
	/*
	 * @ ensures \result == (\forall int i; i > 0 & i <= getSize(); getField(i)
	 * != Mark.EMPTY); pure
	 */ public boolean isFull() {
		return IntStream.range(0, size * size * size).allMatch(i -> getField(i) != Mark.EMPTY);
	}
	
	/**
	 * Determines if the board has a winner and returns the winner
	 * 
	 * @return the winning mark or Mark.Empty if there is no winner
	 */
	/*
	 * @ ensures (\forall int i; i > 0 & i <= getWinConditions().size();
	 * IntStream.of(getWinConditions().get(i)).anyMatch(j -> j == Mark.EMPTY)) &
	 * \result == Mark.EMPTY; pure
	 */ public int findWinner() {
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
	// @ ensures (\forall int i; i > 0 & i < getSize(); getField(i) ==
	// Mark.EMPTY);
	public void reset() {
		Arrays.fill(fields, Mark.EMPTY);
	}
	
	/**
	 * getter size of the board
	 * 
	 * @return the size of the board
	 */
	/* @ pure */ public int getSize() {
		return size;
	}
	
	/**
	 * getter win conditions
	 * 
	 * @return a list of arrays with all possible winning combinations
	 */
	/* @ pure */ public ArrayList<int[]> getWinConditions() {
		return wincons;
	}
	
	/**
	 * getter length needed to win.
	 * 
	 * @return length needed to win
	 */
	/* @ pure */ public int getWinLength() {
		return winLength;
	}
	
	/**
	 * getter array with a copy of all fields
	 * 
	 * @return an copy of the fields array
	 */
	// @ ensures (\forall int i; i > 0 & i < getSize(); getField(i) ==
	// \result[i]);
	/* @ pure */ public int[] getFieldsClone() {
		return fields.clone();
	}
	
	/* @ pure */ public int[] getFields() {
		return fields;
	}
	
	@Override
	
	/**
	 * returns a string representation of the board in a format that can
	 * directly be used for a TUI.
	 */
	// ensures looks like a board
	/* @ pure */ public String toString() {
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
	
	
	/**
	 * Getter field length
	 * 
	 * @return the total number of fields
	 */
	/* @ pure */ public int getFieldLength() {
		return size * size * size;
	}
}
