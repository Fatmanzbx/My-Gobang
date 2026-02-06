/**
 * Game mode where the player plays as Black (first move).
 * AI plays as White.
 * Ban hand rules apply to the player's black stones.
 */
public class PlayBlack extends DrawChessBoard {
    
    public PlayBlack() {
        mode = BLACK;
    }
    
    @Override
    public void placeStone(int row, int col) {
        // Check for foul move (ban hand) before placing
        if (banHandEnabled && isFoulMove(row, col)) {
            showFoulWarning("This move violates ban hand rules (禁手)");
            return; // Don't place the stone
        }
        
        // Player places black stone
        moveCount++;
        chessDisplay[row][col] = new Chessman(BLACK, true);
        board.setCell(row, col, BLACK);
        moveHistory[moveCount] = new Stone(BLACK, row, col);
        
        checkGameEnd();
        repaint();
        
        if (gameResult == 0) {
            if (moveCount > 1) {
                // AI makes a move
                moveCount++;
                startAIMove(BLACK);
            } else {
                // First move: AI places stone adjacent to player's stone, toward center
                int aiRow = 8;
                int aiCol = 8;
                int[] rowOffsets = {1, 1, -1, -1, 0, 0, 1, -1};
                int[] colOffsets = {1, -1, -1, 1, 1, -1, 0, 0};
                
                for (int i = 0; i < 8; i++) {
                    int newRow = row + rowOffsets[i];
                    int newCol = col + colOffsets[i];
                    // Prefer positions closer to center
                    if (Math.abs(newRow - 7) <= Math.abs(row - 7) && 
                        Math.abs(newCol - 7) <= Math.abs(col - 7)) {
                        aiRow = newRow;
                        aiCol = newCol;
                        break;
                    }
                }
                
                moveCount++;
                chessDisplay[aiRow][aiCol] = new Chessman(WHITE, true);
                board.setCell(aiRow, aiCol, WHITE);
                moveHistory[moveCount] = new Stone(WHITE, aiRow, aiCol);
                checkGameEnd();
                repaint();
            }
        }
    }
}
