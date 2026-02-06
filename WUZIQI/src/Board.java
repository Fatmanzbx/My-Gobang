/**
 * Represents the game board state.
 * Tracks stone positions and determines win conditions.
 */
public class Board {
    
    public static final int BOARD_SIZE = 15;
    public static final int BLACK = -1;
    public static final int WHITE = 1;
    public static final int EMPTY = 0;
    public static final int WIN_LENGTH = 5;
    
    private final int[][] grid = new int[BOARD_SIZE][BOARD_SIZE];
    
    /**
     * Gets the stone color at the specified position.
     * @param row The row index
     * @param col The column index
     * @return The color at that position (BLACK, WHITE, or EMPTY)
     */
    public int getCell(int row, int col) {
        return grid[row][col];
    }
    
    /**
     * Sets a stone at the specified position.
     * @param row The row index
     * @param col The column index
     * @param color The stone color to place
     */
    public void setCell(int row, int col, int color) {
        grid[row][col] = color;
    }
    
    /**
     * Checks for a winner on the board.
     * @return BLACK if black wins, WHITE if white wins, EMPTY if no winner
     */
    public int checkWinner() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Check diagonal (top-left to bottom-right)
                int sum1 = countDiagonal(i, j, 1, 1);
                
                // Check diagonal (top-right to bottom-left)
                int sum2 = countDiagonal(i, j, -1, 1);
                
                // Check horizontal
                int sum3 = countLine(i, j, 0, 1);
                
                // Check vertical
                int sum4 = countLine(i, j, 1, 0);
                
                if (sum1 == -WIN_LENGTH || sum2 == -WIN_LENGTH || 
                    sum3 == -WIN_LENGTH || sum4 == -WIN_LENGTH) {
                    return BLACK;
                }
                if (sum1 == WIN_LENGTH || sum2 == WIN_LENGTH || 
                    sum3 == WIN_LENGTH || sum4 == WIN_LENGTH) {
                    return WHITE;
                }
            }
        }
        return EMPTY;
    }
    
    /**
     * Counts stones in a diagonal line centered at the given position.
     */
    private int countDiagonal(int row, int col, int rowDir, int colDir) {
        int sum = 0;
        try {
            for (int k = -2; k <= 2; k++) {
                sum += grid[row + k * rowDir][col + k * colDir];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Position is near edge, ignore
        }
        return sum;
    }
    
    /**
     * Counts stones in a horizontal or vertical line.
     */
    private int countLine(int row, int col, int rowDir, int colDir) {
        int sum = 0;
        int start = rowDir != 0 ? Math.max(-row, -2) : Math.max(-col, -2);
        
        for (int k = start; k <= 2; k++) {
            int newRow = row + k * rowDir;
            int newCol = col + k * colDir;
            if (newRow >= BOARD_SIZE || newCol >= BOARD_SIZE) break;
            sum += grid[newRow][newCol];
        }
        return sum;
    }
}

