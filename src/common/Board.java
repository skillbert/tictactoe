package common;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Board {
    public static final int DIM = 4;
    private Mark[] fields;
    
    public Board() {
        fields = new Mark[DIM*DIM*DIM];
        Arrays.fill(fields, Mark.EMPTY);
    }
    
    public Board deepCopy() {
        Board board = new Board();
        IntStream.range(0, DIM*DIM*DIM).forEach(i -> board.setField(i, this.getField(i)));
        return board;
    }
    
    public int index(int row, int column, int height) {
        return row + (column * DIM) + (height * DIM * DIM);
    }
    
    
    public boolean isField(int index) {
        return 0 <= index && index < DIM * DIM * DIM;
    }
    
    public Mark getField(int index) {
        return fields[index];
    }
    
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
    
    public boolean isFull(){
        return IntStream.range(0, DIM*DIM*DIM).allMatch(i -> getField(i) != Mark.EMPTY);
    }
    
    public boolean hasWinner(){
        return isWinner(Mark.RED) || isWinner(Mark.YELLOW);
    }
    
    public boolean isWinner(Mark m){
        return hasRow(m) || hasColumn(m) || hasTower(m);
    }
    
    public boolean hasRow(Mark m) {
        return IntStream.range(0, DIM)
                .anyMatch(i -> IntStream.range(0, DIM)
                        .anyMatch(j -> IntStream.range(0, DIM)
                                .allMatch(k -> getField(index(j, k, i)) == m)));
    }
    
    public boolean hasColumn(Mark m) {
        return IntStream.range(0, DIM)
                .anyMatch(i -> IntStream.range(0, DIM)
                        .anyMatch(j -> IntStream.range(0, DIM)
                                .allMatch(k -> getField(index(k, j, i)) == m)));
    }
    
    public boolean hasTower(Mark m) {
        return IntStream.range(0, DIM)
                .anyMatch(i -> IntStream.range(0, DIM)
                        .anyMatch(j -> IntStream.range(0, DIM)
                                .allMatch(k -> getField(index(i, j, k)) == m)));
    }
    
    public boolean hasRowColumnTower(Mark m) {
        return IntStream.range(0, DIM)
                .anyMatch(i -> IntStream.range(0, DIM)
                        .anyMatch(j -> 
                            IntStream.range(0, DIM).allMatch(k -> getField(index(j, k, i)) == m) || 
                            IntStream.range(0, DIM).allMatch(k -> getField(index(k, j, i)) == m) ||
                            IntStream.range(0, DIM).allMatch(k -> getField(index(i, j, k)) == m)));
    }
    
    public boolean gameOver() {
        return this.isFull() || this.hasWinner();
    }
}
