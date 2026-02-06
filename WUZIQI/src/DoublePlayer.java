/**
 * Two-player game mode.
 * Players alternate placing black and white stones.
 * Ban hand rules apply to black stones.
 */
public class DoublePlayer extends DrawChessBoard {
    
    @Override
    public void placeStone(int row, int col) {
        moveCount++;
        
        // Alternate colors: odd moves are black, even moves are white
        int color = ((1 + moveCount) % 2) * 2 - 1;
        
        // Check for foul move if placing black stone
        if (color == BLACK && banHandEnabled && isFoulMove(row, col)) {
            moveCount--; // Undo the move count increment
            showFoulWarning("This move violates ban hand rules (禁手)");
            return; // Don't place the stone
        }
        
        chessDisplay[row][col] = new Chessman(color, true);
        board.setCell(row, col, color);
        moveHistory[moveCount] = new Stone(color, row, col);
        setLastMove(row, col);
        
        checkGameEnd();
        repaint();
    }
}
