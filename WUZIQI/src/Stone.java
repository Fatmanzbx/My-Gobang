/**
 * Represents a stone (chess piece) placed on the board.
 * Used for recording game history.
 */
public class Stone {
    
    private final int color;  // 1 = white, -1 = black
    private final int row;
    private final int col;
    
    /**
     * Creates a new stone.
     * @param color The stone color (1 for white, -1 for black)
     * @param row The row position on the board
     * @param col The column position on the board
     */
    public Stone(int color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public int getColor() {
        return color;
    }
}

