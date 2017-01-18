package common;

public abstract class Player {
    private String name;
    private Mark mark;
    private PlayerState state;
    
    public Player(String name, Mark mark) {
        this.name = name;
        this.mark = mark;
    }
    
    public String getName() {
        return name;
    }
    
    public Mark getMark() {
        return mark;
    }
    
    public abstract int determineMove(Board board);
    
    public void makeMove(Board board) {
        int choice = determineMove(board);
        board.setField(choice, getMark());
    }
}
