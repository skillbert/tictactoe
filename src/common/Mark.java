package common;

public enum Mark {
    EMPTY, RED, YELLOW;
    
    public String toString() {
        return this.name().toLowerCase();
    }
}
