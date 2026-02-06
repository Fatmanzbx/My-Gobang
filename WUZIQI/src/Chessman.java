/**
 * Represents a chessman (stone) for display purposes.
 * Tracks the color and placement state of a stone on the board.
 */
public class Chessman {
    
    public static final int WHITE = 1;
    public static final int BLACK = -1;
    
    private int color;      // 1 = white, -1 = black
    private boolean placed; // Whether the stone has been placed
    
    /**
     * Creates a new chessman.
     * @param color The stone color (1 for white, -1 for black)
     * @param placed Whether the stone is placed on the board
     */
    public Chessman(int color, boolean placed) {
        this.color = color;
        this.placed = placed;
    }
    
    public boolean getPlaced() {
        return placed;
    }
    
    public void setPlaced(boolean placed) {
        this.placed = placed;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
}
