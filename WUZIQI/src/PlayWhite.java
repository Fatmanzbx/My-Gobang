/**
 * Game mode where the player plays as White (second move).
 * AI plays as Black and makes the first move.
 */
public class PlayWhite extends DrawChessBoard {
    
    public PlayWhite() {
        mode = WHITE;
        
        // AI makes the first move in the center
        chessDisplay[7][7] = new Chessman(BLACK, true);
        board.setCell(7, 7, BLACK);
        moveCount++;
        moveHistory[moveCount] = new Stone(BLACK, 7, 7);
        setLastMove(7, 7);
    }
    
    @Override
    public void placeStone(int row, int col) {
        // Player places white stone
        moveCount++;
        chessDisplay[row][col] = new Chessman(WHITE, true);
        board.setCell(row, col, WHITE);
        moveHistory[moveCount] = new Stone(WHITE, row, col);
        setLastMove(row, col);
        
        checkGameEnd();
        repaint();
        
        if (gameResult == 0) {
            // AI makes a move
            moveCount++;
            startAIMove(WHITE);
        }
    }
}
