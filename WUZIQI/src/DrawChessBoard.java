import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Chess board panel that handles drawing and user interaction.
 * This is the main game panel where the board is displayed and clicks are processed.
 */
public class DrawChessBoard extends JPanel implements MouseListener, MouseMotionListener {
    
    // Stone color constants
    protected static final int BLACK = -1;
    protected static final int WHITE = 1;
    protected static final int EMPTY = 0;
    protected static final int DRAW = 2;
    
    // Board dimensions
    protected static final int BOARD_SIZE = 15;
    
    // Ban hand (foul) settings - can be toggled
    protected boolean banHandEnabled = true;
    
    // Board state
    protected Board board = new Board();
    protected Chessman[][] chessDisplay = new Chessman[BOARD_SIZE][BOARD_SIZE];
    protected Stone[] moveHistory = new Stone[226];
    protected int moveCount = 0;
    protected int gameResult = 0;  // 0 = ongoing, -1 = black wins, 1 = white wins
    protected int mode = 2;  // Game mode identifier
    
    // For replay navigation
    private int replayOffset = 0;
    
    // Drawing calculations
    private int cellHeight;
    private int cellWidth;
    private int boardOffsetX;
    private int boardOffsetY;

    // Last move marker
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;
    
    // Images
    protected Image boardImage;
    protected AI ai;
    protected volatile boolean aiThinking = false;

    /**
     * Constructor for starting a new game.
     */
    public DrawChessBoard() {
        loadBoardImage();
        addMouseListener(this);
    }

    /**
     * Constructor for viewing a recorded game.
     * @param stones Array of stones representing the recorded game
     */
    public DrawChessBoard(Stone[] stones) {
        loadBoardImage();
        addMouseListener(this);
        this.moveHistory = stones;
        
        // Replay all moves to reconstruct board state
        for (int i = 1; i < stones.length; i++) {
            if (stones[i] == null) {
                this.moveCount = i - 1;
                break;
            }
            Stone stone = stones[i];
            chessDisplay[stone.getRow()][stone.getCol()] = new Chessman(stone.getColor(), true);
            board.setCell(stone.getRow(), stone.getCol(), stone.getColor());
        }
        updateLastMoveMarker();
        setLayout(null);
    }

    /**
     * Loads the board background image.
     */
    private void loadBoardImage() {
        boardImage = Toolkit.getDefaultToolkit().getImage(
            DrawChessBoard.class.getResource("/qipan.jpg")
        );
        if (boardImage == null) {
            System.err.println("Error: Board image not found");
        }
    }

    /**
     * Undo the last k moves.
     * @param k Number of moves to undo
     */
    public void undoMove(int k) {
        if (moveCount >= k) {
            for (int i = 0; i < k; i++) {
                Stone stone = moveHistory[moveCount - i];
                board.setCell(stone.getRow(), stone.getCol(), 0);
                chessDisplay[stone.getRow()][stone.getCol()] = null;
                moveHistory[moveCount - i] = null;
            }
            moveCount -= k;
            gameResult = 0;
            loadBoardImage();
            updateLastMoveMarker();
            repaint();
        }
    }

    /**
     * Navigate to previous move in replay mode.
     */
    public void goBack() {
        if (moveCount > replayOffset) {
            Stone stone = moveHistory[moveCount - replayOffset];
            board.setCell(stone.getRow(), stone.getCol(), 0);
            chessDisplay[stone.getRow()][stone.getCol()] = null;
            replayOffset++;
            updateLastMoveMarker();
            repaint();
        }
    }

    /**
     * Navigate to next move in replay mode.
     */
    public void goForward() {
        if (replayOffset > 0) {
            Stone stone = moveHistory[moveCount - replayOffset + 1];
            board.setCell(stone.getRow(), stone.getCol(), stone.getColor());
            chessDisplay[stone.getRow()][stone.getCol()] = new Chessman(stone.getColor(), true);
            replayOffset--;
            updateLastMoveMarker();
            repaint();
        }
    }
    
    /**
     * Restores a move from a saved game (used when loading and continuing).
     * @param stone The stone to restore
     */
    public void restoreMove(Stone stone) {
        moveCount++;
        chessDisplay[stone.getRow()][stone.getCol()] = new Chessman(stone.getColor(), true);
        board.setCell(stone.getRow(), stone.getCol(), stone.getColor());
        moveHistory[moveCount] = stone;
        setLastMove(stone.getRow(), stone.getCol());
        checkGameEnd();
        repaint();
    }

    /**
     * Places a stone at the given position.
     * Override in subclasses to implement game mode specific logic.
     * @param row The row to place the stone
     * @param col The column to place the stone
     */
    public void placeStone(int row, int col) {
        // To be overridden by subclasses
    }
    
    /**
     * Checks if a move is a foul (ban hand) for black.
     * @param row The row position
     * @param col The column position
     * @return true if the move is forbidden
     */
    protected boolean isFoulMove(int row, int col) {
        if (!banHandEnabled) {
            return false;
        }
        
        // Get board state as 2D array
        int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardState[i][j] = board.getCell(i, j);
            }
        }
        
        return AI.isFoulMove(row, col, boardState);
    }
    
    /**
     * Shows a foul warning message.
     */
    protected void showFoulWarning(String foulType) {
        JOptionPane.showMessageDialog(this,
            "Forbidden move (禁手): " + foulType + "\nBlack cannot make this move!",
            "Foul Move",
            JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Enables or disables ban hand rules.
     */
    public void setBanHandEnabled(boolean enabled) {
        this.banHandEnabled = enabled;
    }

    /**
     * Checks for a winner and updates the game state.
     */
    protected void checkGameEnd() {
        gameResult = board.checkWinner();
        if (gameResult == BLACK) {
            boardImage = Toolkit.getDefaultToolkit().getImage(
                DrawChessBoard.class.getResource("/Blackwin.jpg")
            );
        } else if (gameResult == WHITE) {
            boardImage = Toolkit.getDefaultToolkit().getImage(
                DrawChessBoard.class.getResource("/Whitewin.jpg")
            );
        } else if (moveCount >= BOARD_SIZE * BOARD_SIZE) {
            gameResult = DRAW;
            boardImage = Toolkit.getDefaultToolkit().getImage(
                DrawChessBoard.class.getResource("/Draw.jpg")
            );
        }
    }

    /**
     * Makes the AI play a move.
     * @param color The AI's stone color
     */
    protected void makeAIMove(int color) {
        int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardState[i][j] = board.getCell(i, j);
            }
        }
        
        ai = new AI(boardState, color);
        int[] result = ai.getResult();
        
        int aiColor = -color;
        chessDisplay[result[0]][result[1]] = new Chessman(aiColor, true);
        board.setCell(result[0], result[1], aiColor);
        moveHistory[moveCount] = new Stone(aiColor, result[0], result[1]);
        setLastMove(result[0], result[1]);
    }

    /**
     * Runs the AI move computation off the UI thread and applies the result on the EDT.
     * @param color The AI's stone color (same convention as makeAIMove)
     */
    protected void startAIMove(int color) {
        if (aiThinking) {
            return;
        }
        aiThinking = true;
        int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardState[i][j] = board.getCell(i, j);
            }
        }

        javax.swing.SwingWorker<int[], Void> worker = new javax.swing.SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                ai = new AI(boardState, color);
                return ai.getResult();
            }

            @Override
            protected void done() {
                try {
                    int[] result = get();
                    int aiColor = -color;
                    chessDisplay[result[0]][result[1]] = new Chessman(aiColor, true);
                    board.setCell(result[0], result[1], aiColor);
                    moveHistory[moveCount] = new Stone(aiColor, result[0], result[1]);
                    setLastMove(result[0], result[1]);
                    checkGameEnd();
                    repaint();
                } catch (Exception e) {
                    // Ignore AI failures; keep UI responsive
                } finally {
                    aiThinking = false;
                }
            }
        };
        worker.execute();
    }

    /**
     * Gets a stone from the move history.
     * @param moveNumber The move number (1-indexed)
     * @return The stone at that move
     */
    public Stone getStone(int moveNumber) {
        return moveHistory[moveNumber];
    }

    /**
     * Gets the total number of moves played.
     * @return The move count
     */
    public int getMoveCount() {
        return moveCount;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Calculate board dimensions
        int imgWidth = boardImage.getWidth(this) - 140;
        int imgHeight = boardImage.getHeight(this) - 280;
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        boardOffsetX = (panelWidth - imgWidth) / 2;
        boardOffsetY = (panelHeight - imgHeight) / 2;
        
        // Draw board background
        g.drawImage(boardImage, 0, 0, null);
        
        // Calculate cell size
        cellWidth = imgWidth / (BOARD_SIZE - 1);
        cellHeight = imgHeight / (BOARD_SIZE - 1);
        
        int marginX = (imgWidth % (BOARD_SIZE - 1)) / 2 + boardOffsetX;
        int marginY = (imgHeight % (BOARD_SIZE - 1)) / 2 + boardOffsetY;
        
        // Draw grid lines
        g.setColor(Color.BLACK);
        for (int i = 0; i < BOARD_SIZE; i++) {
            // Horizontal lines
            g.drawLine(marginX, marginY + i * cellHeight, 
                      panelWidth - marginX, marginY + i * cellHeight);
            // Vertical lines
            g.drawLine(marginX + i * cellWidth, marginY, 
                      marginX + i * cellWidth, panelHeight - marginY);
        }
        
        // Draw stones
        drawStones(g);
    }

    /**
     * Draws all stones on the board.
     */
    private void drawStones(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int stoneSize = 20;
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (chessDisplay[i][j] != null && chessDisplay[i][j].getPlaced()) {
                    int posX = boardOffsetX + i * cellWidth;
                    int posY = boardOffsetY + j * cellHeight;
                    
                    float[] fractions = new float[]{0f, 1f};
                    RadialGradientPaint paint;
                    
                    if (chessDisplay[i][j].getColor() == WHITE) {
                        // White stone with gradient
                        Color[] colors = new Color[]{Color.WHITE, Color.YELLOW};
                        paint = new RadialGradientPaint(posX, posY, 50, fractions, colors);
                    } else {
                        // Black stone with gradient
                        Color[] colors = new Color[]{Color.BLUE, Color.BLACK};
                        paint = new RadialGradientPaint(
                            posX - stoneSize / 2f, posY - stoneSize / 2f, 
                            40, fractions, colors
                        );
                    }
                    
                    g2d.setPaint(paint);
                    g2d.fillOval(
                        posX - stoneSize / 2, posY - stoneSize / 2, 
                        stoneSize, stoneSize
                    );
                }
            }
        }

        if (lastMoveRow >= 0 && lastMoveCol >= 0) {
            int posX = boardOffsetX + lastMoveRow * cellWidth;
            int posY = boardOffsetY + lastMoveCol * cellHeight;
            int markSize = 6;
            g2d.setColor(Color.RED);
            g2d.fillOval(posX - markSize / 2, posY - markSize / 2, markSize, markSize);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameResult != 0 || aiThinking) return; // Game ended or AI thinking
        
        int clickX = e.getX();
        int clickY = e.getY();

        if (cellWidth <= 0 || cellHeight <= 0) return;

        float fx = (clickX - boardOffsetX) / (float) cellWidth;
        float fy = (clickY - boardOffsetY) / (float) cellHeight;
        int row = Math.round(fx);
        int col = Math.round(fy);

        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;

        int gridX = row * cellWidth + boardOffsetX;
        int gridY = col * cellHeight + boardOffsetY;
        int dx = clickX - gridX;
        int dy = clickY - gridY;
        int maxDist = Math.min(cellWidth, cellHeight) / 2;
        if (dx * dx + dy * dy > maxDist * maxDist) return;

        if (board.getCell(row, col) == 0) {
            placeStone(row, col);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    protected void setLastMove(int row, int col) {
        lastMoveRow = row;
        lastMoveCol = col;
    }

    private void updateLastMoveMarker() {
        int index = moveCount - replayOffset;
        if (index >= 1 && moveHistory[index] != null) {
            setLastMove(moveHistory[index].getRow(), moveHistory[index].getCol());
        } else {
            lastMoveRow = -1;
            lastMoveCol = -1;
        }
    }
}
