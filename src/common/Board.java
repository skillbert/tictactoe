package common;

import java.util.Arrays;
import java.util.stream.IntStream;

/** 
 * Board for the Connect3D game. Module 2 Programming project.
 * @author Maurits van der Vijgh
 * @version $0.1$
 */
public class Board {
    public static final int DIM = 4;
    private Mark[] fields;
    
    /**
     * fields represent the board. column/row to index.
     *      c1  c2  c3  c4
     * r1   0   4   8   12
     * r2   1   5   9   13
     * r3   2   6   10  14
     * r4   3   7   11  15
     * 
     * DIM of these 2D grids are stacked on top of each other to achieve a 3D board
     * to get the index of a certain row and column combination you have to add 16 for each layer
     * on top of the base one.
     */
    public Board() {
        fields = new Mark[DIM*DIM*DIM];
        Arrays.fill(fields, Mark.EMPTY);
    }
    
    /** 
     * Returns a new Board object with the same fields marked as the current board
     * @return
     */
    public Board deepCopy() {
        Board board = new Board();
        IntStream.range(0, DIM*DIM*DIM).forEach(i -> board.setField(i, this.getField(i)));
        return board;
    }
    
    /**
     * Return the index of the field at row, column, height.
     * @param row
     * @param column
     * @param height
     * @return the selected field
     */
    public int index(int row, int column, int height) {
        return row + (column * DIM) + (height * DIM * DIM);
    }
    
    /**
     * Returns true if the field index is within range.
     * @param index
     * @return true if the field exists
     */
    public boolean isField(int index) {
        return 0 <= index && index < DIM * DIM * DIM;
    }
    
    /**
     * Returns Mark at index in fields
     * @param index
     * @return Mark
     */
    public Mark getField(int index) {
        return fields[index];
    }
    
    /**
     * Sets index index in fields to Mark m
     * @param index
     * @param m
     */
    public void setField(int index, Mark m) {
        fields[index] = m;
    }
    
    /**
     * Returns true if the field below the chosen field does not exist or is filled.
     * @param index
     * @return true if the field is available
     */
    public boolean isAvailableField(int index) {
        return getField(index) == Mark.EMPTY && (!isField(index - DIM*DIM) || getField(index - DIM*DIM) != Mark.EMPTY);
    }
         
    /**
     * Returns true if mark m controls a row, column or a tower.
     * @param m
     * @return
     */
    public boolean hasRowColumnTower(Mark m) {
        return IntStream.range(0, DIM)
                .anyMatch(i -> IntStream.range(0, DIM)
                        .anyMatch(j -> 
                            IntStream.range(0, DIM).allMatch(k -> getField(index(j, k, i)) == m) || 
                            IntStream.range(0, DIM).allMatch(k -> getField(index(k, j, i)) == m) ||
                            IntStream.range(0, DIM).allMatch(k -> getField(index(i, j, k)) == m)));
    }
    
    /**
     * Returns true if mark m controls a diagonal
     * @param m
     * @return
     */
    public boolean hasDiagonal(Mark m) {
        return IntStream.range(0, DIM)
            .anyMatch(i -> 
                IntStream.range(0, DIM).allMatch(j -> getField(index(i, j, j)) == m) ||
                IntStream.range(0, DIM).allMatch(j -> getField(index(j, i, j)) == m) ||
                IntStream.range(0, DIM).allMatch(j -> getField(index(j, j, i)) == m) ||
                IntStream.range(0, DIM).allMatch(j -> getField(index(i, j, -j)) == m) ||
                IntStream.range(0, DIM).allMatch(j -> getField(index(j, i, -j)) == m) ||
                IntStream.range(0, DIM).allMatch(j -> getField(index(j, -j, i)) == m)) ||
            IntStream.range(0, DIM).allMatch(j -> getField(index(j, j, j)) == m) ||
            IntStream.range(0, DIM).allMatch(j -> getField(index(-j, j, j)) == m) ||
            IntStream.range(0, DIM).allMatch(j -> getField(index(j, -j, j)) == m) ||
            IntStream.range(0, DIM).allMatch(j -> getField(index(-j, -j, j)) == m);
    }
    
    /**
     * @return true if the board is full
     */
    public boolean isFull(){
        return IntStream.range(0, DIM*DIM*DIM).allMatch(i -> getField(i) != Mark.EMPTY);
    }
    
    /**
     * Returns true if Mark m has won. A mark wins if it has at least one row, column, tower or diagonal.
     * @param m Mark of the player
     * @return true if mark is a winner
     */
    public boolean isWinner(Mark m){
        return hasRowColumnTower(m) || hasDiagonal(m);
    }
    
    /**
     * Returns true if a mark has won.
     * @return if the board has a winner
     */
    public boolean hasWinner(){
        return isWinner(Mark.RED) || isWinner(Mark.YELLOW);
    }
    
    /**
     * Returns true if the board is either full or it has a winner
     * @return true if the game is over
     */
    public boolean gameOver() {
        return this.isFull() || this.hasWinner();
    }
    
    /**
     * Resets the board
     */
    public void reset() {
        Arrays.fill(fields, Mark.EMPTY);
    }
    
    public String toString() {
        String str = "";
        for (int i = 0; i < DIM; i++) {
            str += "\n" + new String(new char[DIM]).replace("\0", " | ----") + " | \n";
            for (int j = 0; j < DIM; j++) {
                str += " | ";
                for (int k = 0; k < DIM; k++) {
                    str += getField(index(i, j, k)).name().substring(0, 1);
                }
            }
            str += " | ";
        }
        str += "\n" + new String(new char[DIM]).replace("\0", " | ----") + " | \n";
        return str;
    }
}
