import java.util.Arrays;

/**
 * Stronger Gobang AI with:
 * - Iterative deepening + time limits per difficulty
 * - Alpha-beta with transposition table
 * - Improved move ordering and pattern-based evaluation
 * - Ban-hand (Renju-style) rules for black: overline, double-three, double-four
 */
public class AI {
    public static final int BOARD_SIZE = 15;
    public static final int BLACK = -1;
    public static final int WHITE = 1;
    public static final int EMPTY = 0;

    private static final int[] DR = {0, 1, 1, 1};
    private static final int[] DC = {1, 1, 0, -1};

    private static int difficulty = 1; // 0 easy, 1 medium, 2 hard

    // Time limits per move (ms)
    private static final int[] TIME_LIMITS_MS = {500, 1000, 2000};
    private static final int[] MAX_DEPTHS = {3, 5, 7};
    private static final int[] MAX_CANDIDATES = {10, 14, 20};

    // Scoring
    private static final int SCORE_WIN = 1_000_000_000;
    private static final int SCORE_OPEN_FOUR = 10_000_000;
    private static final int SCORE_FOUR = 1_000_000;
    private static final int SCORE_OPEN_THREE = 100_000;
    private static final int SCORE_THREE = 10_000;
    private static final int SCORE_OPEN_TWO = 1_000;
    private static final int SCORE_TWO = 100;
    private static final int SCORE_NEIGHBOR = 5;

    // Patterns on normalized line: '1' = self, '0' = empty, '2' = block
    private static final String OPEN_FOUR_PATTERN = "011110";
    private static final String[] OPEN_THREE_PATTERNS = {"01110", "010110", "011010"};

    // Zobrist hashing
    private static final long[][][] ZOBRIST = new long[BOARD_SIZE][BOARD_SIZE][2];
    static {
        java.util.Random r = new java.util.Random(1337);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                ZOBRIST[i][j][0] = r.nextLong(); // BLACK
                ZOBRIST[i][j][1] = r.nextLong(); // WHITE
            }
        }
    }

    private static final int TT_MAX = 200_000;
    private final java.util.HashMap<Long, TTEntry> tt = new java.util.HashMap<>(TT_MAX * 2);

    private final int[][] board;
    private final int aiColor;
    private long zobristKey;
    private int stoneCount = 0;

    // Cached line scores for faster evaluation
    private final int[][] rowScore = new int[2][BOARD_SIZE];
    private final int[][] colScore = new int[2][BOARD_SIZE];
    private final int[][] diag1Score = new int[2][BOARD_SIZE * 2 - 1];
    private final int[][] diag2Score = new int[2][BOARD_SIZE * 2 - 1];

    private int nodesEvaluated = 0;
    private long endTimeMs = 0;
    private boolean timeUp = false;

    public AI(int[][] boardState, int playerColor) {
        this.board = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(boardState[i], 0, this.board[i], 0, BOARD_SIZE);
        }
        if (playerColor != BLACK && playerColor != WHITE) {
            throw new IllegalArgumentException("playerColor must be BLACK or WHITE");
        }
        this.aiColor = -playerColor;
        this.zobristKey = computeHash();
        this.stoneCount = computeStoneCount();
        rebuildAllScores();
    }

    public static void setDifficulty(int level) {
        if (level < 0) level = 0;
        if (level > 2) level = 2;
        difficulty = level;
        System.out.println("AI difficulty set to " + getDifficultyName() +
            " (time limit: " + TIME_LIMITS_MS[level] + "ms, max depth: " + MAX_DEPTHS[level] + ")");
    }

    public static int getDifficulty() {
        return difficulty;
    }

    public static String getDifficultyName() {
        switch (difficulty) {
            case 0: return "Easy";
            case 1: return "Medium";
            case 2: return "Hard";
            default: return "Unknown";
        }
    }

    public int[] getResult() {
        nodesEvaluated = 0;
        long start = System.currentTimeMillis();
        endTimeMs = start + TIME_LIMITS_MS[difficulty];
        timeUp = false;

        // First move: center
        if (isBoardEmpty()) {
            return new int[]{7, 7};
        }

        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int[] preferredMove = null;

        for (int depth = 1; depth <= MAX_DEPTHS[difficulty]; depth++) {
            int[] move = findBestMove(depth, preferredMove);
            if (timeUp || move == null) {
                break;
            }
            preferredMove = move;
            bestMove = move;
            bestScore = lastRootScore;
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("AI (" + getDifficultyName() + ") evaluated " + nodesEvaluated +
            " positions in " + elapsed + "ms, score " + bestScore);

        return bestMove != null ? bestMove : new int[]{7, 7};
    }

    // ==================== ROOT SEARCH ====================

    private int lastRootScore = Integer.MIN_VALUE;

    private int[] findBestMove(int depth, int[] preferredMove) {
        int[] tactical = findImmediateMove();
        if (tactical != null) {
            return tactical;
        }

        int[][] candidates = getCandidateMoves(aiColor, MAX_CANDIDATES[difficulty], preferredMove);
        if (candidates.length == 0) {
            return null;
        }

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        int alpha = Integer.MIN_VALUE / 2;
        int beta = Integer.MAX_VALUE / 2;

        for (int[] move : candidates) {
            if (isTimeUp()) break;
            int r = move[0];
            int c = move[1];

            if (aiColor == BLACK && isFoulMoveInternal(r, c)) {
                continue;
            }

            makeMove(r, c, aiColor);
            int score = -alphaBeta(-aiColor, depth - 1, -beta, -alpha);
            undoMove(r, c, aiColor);

            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{r, c};
            }

            if (score > alpha) alpha = score;
            if (alpha >= beta) {
                break;
            }
        }

        lastRootScore = bestScore;
        return bestMove;
    }

    private int alphaBeta(int color, int depth, int alpha, int beta) {
        if (isTimeUp()) {
            return evaluate(color);
        }
        nodesEvaluated++;

        int winner = checkWinner();
        if (winner != EMPTY) {
            return winner == color ? SCORE_WIN : -SCORE_WIN;
        }

        if (depth <= 0) {
            return evaluate(color);
        }

        long key = zobristKey ^ (color == BLACK ? 1L : 2L);
        TTEntry entry = tt.get(key);
        if (entry != null && entry.depth >= depth) {
            if (entry.flag == TTEntry.EXACT) return entry.value;
            if (entry.flag == TTEntry.LOWER && entry.value > alpha) alpha = entry.value;
            if (entry.flag == TTEntry.UPPER && entry.value < beta) beta = entry.value;
            if (alpha >= beta) return entry.value;
        }

        int alpha0 = alpha;
        int[][] candidates = getCandidateMoves(color, MAX_CANDIDATES[difficulty],
            entry != null ? entry.bestMove : null);
        if (candidates.length == 0) {
            return evaluate(color);
        }

        int bestScore = Integer.MIN_VALUE / 2;
        int[] bestMove = null;

        for (int[] move : candidates) {
            if (isTimeUp()) break;
            int r = move[0];
            int c = move[1];

            if (color == BLACK && isFoulMoveInternal(r, c)) {
                continue;
            }

            makeMove(r, c, color);
            int score = -alphaBeta(-color, depth - 1, -beta, -alpha);
            undoMove(r, c, color);

            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{r, c};
            }

            if (score > alpha) alpha = score;
            if (alpha >= beta) break;
        }

        int flag = TTEntry.EXACT;
        if (bestScore <= alpha0) flag = TTEntry.UPPER;
        else if (bestScore >= beta) flag = TTEntry.LOWER;

        if (tt.size() > TT_MAX) tt.clear();
        if (bestMove != null) {
            tt.put(key, new TTEntry(bestScore, depth, flag, bestMove));
        }

        return bestScore;
    }

    // ==================== TACTICAL CHECKS ====================

    private int[] findImmediateMove() {
        int[] blockWin = null;
        int[] win = null;
        int[] openFour = null;
        int[] blockOpenFour = null;
        int[] doubleThreat = null;
        int[] blockDoubleThreat = null;
        int neighborDist = Math.max(2, getNeighborDistance());

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] != EMPTY || !hasNeighbor(r, c, neighborDist)) continue;

                if (aiColor == BLACK && isFoulMoveInternal(r, c)) continue;

                board[r][c] = aiColor;
                if (isWinningPosition(r, c, aiColor)) {
                    win = new int[]{r, c};
                } else {
                    ThreatCount tc = getThreatCount(r, c, aiColor);
                    if (tc.openFour > 0) openFour = new int[]{r, c};
                    if (tc.isDoubleThreat()) doubleThreat = new int[]{r, c};
                }
                board[r][c] = EMPTY;

                board[r][c] = -aiColor;
                if (isWinningPosition(r, c, -aiColor)) {
                    blockWin = new int[]{r, c};
                } else {
                    ThreatCount otc = getThreatCount(r, c, -aiColor);
                    if (otc.openFour > 0) blockOpenFour = new int[]{r, c};
                    if (otc.isDoubleThreat()) blockDoubleThreat = new int[]{r, c};
                }
                board[r][c] = EMPTY;
            }
        }

        if (win != null) return win;
        if (blockWin != null) return blockWin;
        if (openFour != null) return openFour;
        if (blockOpenFour != null) return blockOpenFour;
        if (blockDoubleThreat != null) return blockDoubleThreat;
        if (doubleThreat != null) return doubleThreat;

        return null;
    }

    // ==================== MOVE GENERATION ====================

    private int[][] getCandidateMoves(int color, int maxCandidates, int[] preferredMove) {
        Move[] moves = new Move[BOARD_SIZE * BOARD_SIZE];
        int count = 0;
        int neighborDist = getNeighborDistance();

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] != EMPTY) continue;
                if (!hasNeighbor(r, c, neighborDist)) continue;

                int score = quickScore(r, c, color);
                moves[count++] = new Move(r, c, score);
            }
        }

        Arrays.sort(moves, 0, count, (a, b) -> Integer.compare(b.score, a.score));

        if (preferredMove != null) {
            for (int i = 0; i < count; i++) {
                if (moves[i].r == preferredMove[0] && moves[i].c == preferredMove[1]) {
                    Move tmp = moves[0];
                    moves[0] = moves[i];
                    moves[i] = tmp;
                    break;
                }
            }
        }

        int resultCount = Math.min(count, maxCandidates);
        int[][] result = new int[resultCount][2];
        for (int i = 0; i < resultCount; i++) {
            result[i][0] = moves[i].r;
            result[i][1] = moves[i].c;
        }
        return result;
    }

    private int quickScore(int row, int col, int color) {
        int score = 0;

        board[row][col] = color;
        if (isWinningPosition(row, col, color)) {
            board[row][col] = EMPTY;
            return SCORE_WIN;
        }
        ThreatCount tc = getThreatCount(row, col, color);
        score += tc.openFour * SCORE_OPEN_FOUR;
        score += tc.four * SCORE_FOUR;
        score += tc.openThree * SCORE_OPEN_THREE;
        board[row][col] = EMPTY;

        board[row][col] = -color;
        if (isWinningPosition(row, col, -color)) {
            board[row][col] = EMPTY;
            return SCORE_WIN - 1;
        }
        ThreatCount otc = getThreatCount(row, col, -color);
        score += otc.openFour * SCORE_OPEN_FOUR;
        score += otc.four * SCORE_FOUR;
        score += otc.openThree * SCORE_OPEN_THREE;
        board[row][col] = EMPTY;

        int centerDist = Math.abs(row - 7) + Math.abs(col - 7);
        score += (14 - centerDist) * 3;

        return score;
    }

    private boolean hasNeighbor(int row, int col, int dist) {
        int r0 = Math.max(0, row - dist);
        int r1 = Math.min(BOARD_SIZE - 1, row + dist);
        int c0 = Math.max(0, col - dist);
        int c1 = Math.min(BOARD_SIZE - 1, col + dist);
        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                if (board[r][c] != EMPTY) return true;
            }
        }
        return false;
    }

    private int getNeighborDistance() {
        if (stoneCount < 6) return 1;
        if (stoneCount < 20) return 2;
        return 3;
    }

    private boolean isBoardEmpty() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] != EMPTY) return false;
            }
        }
        return true;
    }

    // ==================== EVALUATION ====================

    private int evaluate(int color) {
        int myScore = evaluateLines(color);
        int oppScore = evaluateLines(-color);
        return myScore - (int)(oppScore * 1.1);
    }

    private int evaluateLines(int color) {
        int idx = colorIndex(color);
        int score = 0;
        for (int r = 0; r < BOARD_SIZE; r++) score += rowScore[idx][r];
        for (int c = 0; c < BOARD_SIZE; c++) score += colScore[idx][c];
        for (int k = 0; k < BOARD_SIZE * 2 - 1; k++) score += diag1Score[idx][k];
        for (int k = 0; k < BOARD_SIZE * 2 - 1; k++) score += diag2Score[idx][k];
        return score;
    }

    private int scoreLine(String line) {
        int s = 0;
        s += countPattern(line, "11111") * SCORE_WIN;
        s += countPattern(line, "011110") * SCORE_OPEN_FOUR;
        s += (countPattern(line, "211110") + countPattern(line, "011112")) * SCORE_FOUR;
        s += countPattern(line, "01110") * SCORE_OPEN_THREE;
        s += (countPattern(line, "010110") + countPattern(line, "011010")) * SCORE_OPEN_THREE;
        s += (countPattern(line, "001110") + countPattern(line, "011100")) * SCORE_THREE;
        s += (countPattern(line, "00110") + countPattern(line, "01100")) * SCORE_OPEN_TWO;
        s += (countPattern(line, "01010") + countPattern(line, "010010")) * SCORE_TWO;
        s += countPattern(line, "010") * SCORE_NEIGHBOR;
        return s;
    }

    private int countPattern(String line, String pattern) {
        int count = 0;
        int idx = line.indexOf(pattern);
        while (idx != -1) {
            count++;
            idx = line.indexOf(pattern, idx + 1);
        }
        return count;
    }

    private String buildLineStringRow(int row, int color) {
        StringBuilder sb = new StringBuilder(BOARD_SIZE);
        for (int c = 0; c < BOARD_SIZE; c++) {
            sb.append(cellToChar(board[row][c], color));
        }
        return sb.toString();
    }

    private String buildLineStringCol(int col, int color) {
        StringBuilder sb = new StringBuilder(BOARD_SIZE);
        for (int r = 0; r < BOARD_SIZE; r++) {
            sb.append(cellToChar(board[r][col], color));
        }
        return sb.toString();
    }

    private String buildLineStringDiag1(int k, int color) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < BOARD_SIZE; r++) {
            int c = k - r;
            if (c >= 0 && c < BOARD_SIZE) {
                sb.append(cellToChar(board[r][c], color));
            }
        }
        return sb.toString();
    }

    private String buildLineStringDiag2(int k, int color) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < BOARD_SIZE; r++) {
            int c = r - k;
            if (c >= 0 && c < BOARD_SIZE) {
                sb.append(cellToChar(board[r][c], color));
            }
        }
        return sb.toString();
    }

    private char cellToChar(int cell, int color) {
        if (cell == color) return '1';
        if (cell == EMPTY) return '0';
        return '2';
    }

    // ==================== THREAT ANALYSIS ====================

    private ThreatCount getThreatCount(int row, int col, int color) {
        ThreatCount tc = new ThreatCount();
        for (int dir = 0; dir < 4; dir++) {
            char[] line = buildLine(row, col, dir, board, color, 4);
            if (containsPatternWithCenter(line, OPEN_FOUR_PATTERN, 4)) {
                tc.openFour++;
            }
            if (hasFourInLine(line, 4)) {
                tc.four++;
            }
            if (hasOpenThree(line, 4)) {
                tc.openThree++;
            }
        }
        return tc;
    }

    private boolean hasOpenThree(char[] line, int centerIdx) {
        return hasOpenThreeStatic(line, centerIdx);
    }

    private static boolean hasFourInLine(char[] line, int centerIdx) {
        // Any 5-window containing center with 4 stones + 1 empty, no block.
        for (int start = 0; start <= line.length - 5; start++) {
            int end = start + 4;
            if (centerIdx < start || centerIdx > end) continue;
            int stones = 0;
            int empties = 0;
            boolean blocked = false;
            for (int i = start; i <= end; i++) {
                if (line[i] == '1') stones++;
                else if (line[i] == '0') empties++;
                else blocked = true;
            }
            if (!blocked && stones == 4 && empties == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPatternWithCenter(char[] line, String pattern, int centerIdx) {
        String s = new String(line);
        int idx = s.indexOf(pattern);
        while (idx != -1) {
            int end = idx + pattern.length() - 1;
            if (centerIdx >= idx && centerIdx <= end && line[centerIdx] == '1') return true;
            idx = s.indexOf(pattern, idx + 1);
        }
        return false;
    }

    // ==================== WIN CHECK ====================

    private boolean isWinningPosition(int row, int col, int color) {
        for (int dir = 0; dir < 4; dir++) {
            if (countConsecutive(row, col, dir, board, color) >= 5) return true;
        }
        return false;
    }

    private int checkWinner() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int color = board[r][c];
                if (color == EMPTY) continue;
                for (int dir = 0; dir < 4; dir++) {
                    int count = countConsecutive(r, c, dir, board, color);
                    if (count >= 5) {
                        if (color == BLACK && count > 5) continue;
                        return color;
                    }
                }
            }
        }
        return EMPTY;
    }

    // ==================== BAN HAND (FOR BLACK) ====================

    public static boolean isFoulMove(int row, int col, int[][] boardState) {
        if (boardState[row][col] != EMPTY) return true;
        boardState[row][col] = BLACK;

        boolean win = false;
        for (int dir = 0; dir < 4; dir++) {
            int count = countConsecutive(row, col, dir, boardState, BLACK);
            if (count == 5) {
                win = true;
                break;
            }
        }

        boolean foul;
        if (!win) {
            foul = checkOverline(row, col, boardState) ||
                   checkDoubleFour(row, col, boardState) ||
                   checkDoubleThree(row, col, boardState);
        } else {
            foul = checkOverline(row, col, boardState);
        }

        boardState[row][col] = EMPTY;
        return foul;
    }

    private boolean isFoulMoveInternal(int row, int col) {
        if (board[row][col] != EMPTY) return true;
        board[row][col] = BLACK;

        boolean win = false;
        for (int dir = 0; dir < 4; dir++) {
            int count = countConsecutive(row, col, dir, board, BLACK);
            if (count == 5) {
                win = true;
                break;
            }
        }

        boolean foul;
        if (!win) {
            foul = checkOverline(row, col, board) ||
                   checkDoubleFour(row, col, board) ||
                   checkDoubleThree(row, col, board);
        } else {
            foul = checkOverline(row, col, board);
        }

        board[row][col] = EMPTY;
        return foul;
    }

    private static boolean checkOverline(int row, int col, int[][] boardState) {
        for (int dir = 0; dir < 4; dir++) {
            if (countConsecutive(row, col, dir, boardState, BLACK) > 5) return true;
        }
        return false;
    }

    private static boolean checkDoubleFour(int row, int col, int[][] boardState) {
        int count = 0;
        for (int dir = 0; dir < 4; dir++) {
            char[] line = buildLine(row, col, dir, boardState, BLACK, 4);
            if (hasFourInLine(line, 4)) count++;
        }
        return count >= 2;
    }

    private static boolean checkDoubleThree(int row, int col, int[][] boardState) {
        int count = 0;
        for (int dir = 0; dir < 4; dir++) {
            char[] line = buildLine(row, col, dir, boardState, BLACK, 4);
            if (hasOpenThreeStatic(line, 4)) count++;
        }
        return count >= 2;
    }

    private static boolean hasOpenThreeStatic(char[] line, int centerIdx) {
        String s = new String(line);
        for (String p : OPEN_THREE_PATTERNS) {
            int idx = s.indexOf(p);
            while (idx != -1) {
                int end = idx + p.length() - 1;
                if (centerIdx >= idx && centerIdx <= end && line[centerIdx] == '1') return true;
                idx = s.indexOf(p, idx + 1);
            }
        }
        return false;
    }

    // ==================== BOARD / LINE HELPERS ====================

    private static int countConsecutive(int row, int col, int dir, int[][] boardState, int color) {
        int count = 1;

        int r = row + DR[dir];
        int c = col + DC[dir];
        while (isValid(r, c) && boardState[r][c] == color) {
            count++;
            r += DR[dir];
            c += DC[dir];
        }

        r = row - DR[dir];
        c = col - DC[dir];
        while (isValid(r, c) && boardState[r][c] == color) {
            count++;
            r -= DR[dir];
            c -= DC[dir];
        }

        return count;
    }

    private static char[] buildLine(int row, int col, int dir, int[][] boardState, int color, int range) {
        int len = range * 2 + 1;
        char[] line = new char[len];
        int center = range;
        for (int i = -range; i <= range; i++) {
            int r = row + i * DR[dir];
            int c = col + i * DC[dir];
            if (!isValid(r, c)) {
                line[center + i] = '2';
            } else {
                int v = boardState[r][c];
                if (v == color) line[center + i] = '1';
                else if (v == EMPTY) line[center + i] = '0';
                else line[center + i] = '2';
            }
        }
        return line;
    }

    private static boolean isValid(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    // ==================== MOVE APPLY/UNDO ====================

    private void makeMove(int row, int col, int color) {
        board[row][col] = color;
        zobristKey ^= ZOBRIST[row][col][color == BLACK ? 0 : 1];
        stoneCount++;
        updateLineScores(row, col);
    }

    private void undoMove(int row, int col, int color) {
        board[row][col] = EMPTY;
        zobristKey ^= ZOBRIST[row][col][color == BLACK ? 0 : 1];
        stoneCount--;
        updateLineScores(row, col);
    }

    private long computeHash() {
        long h = 0L;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int v = board[r][c];
                if (v == BLACK) h ^= ZOBRIST[r][c][0];
                else if (v == WHITE) h ^= ZOBRIST[r][c][1];
            }
        }
        return h;
    }

    private int computeStoneCount() {
        int count = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] != EMPTY) count++;
            }
        }
        return count;
    }

    private int colorIndex(int color) {
        return color == BLACK ? 0 : 1;
    }

    private void rebuildAllScores() {
        for (int color : new int[]{BLACK, WHITE}) {
            int idx = colorIndex(color);
            for (int r = 0; r < BOARD_SIZE; r++) {
                rowScore[idx][r] = scoreLine(buildLineStringRow(r, color));
            }
            for (int c = 0; c < BOARD_SIZE; c++) {
                colScore[idx][c] = scoreLine(buildLineStringCol(c, color));
            }
            for (int k = 0; k <= (BOARD_SIZE - 1) * 2; k++) {
                String line = buildLineStringDiag1(k, color);
                diag1Score[idx][k] = line.length() >= 5 ? scoreLine(line) : 0;
            }
            for (int k = -(BOARD_SIZE - 1); k <= BOARD_SIZE - 1; k++) {
                String line = buildLineStringDiag2(k, color);
                int idx2 = k + (BOARD_SIZE - 1);
                diag2Score[idx][idx2] = line.length() >= 5 ? scoreLine(line) : 0;
            }
        }
    }

    private void updateLineScores(int row, int col) {
        int diag1 = row + col;
        int diag2 = row - col + (BOARD_SIZE - 1);
        for (int color : new int[]{BLACK, WHITE}) {
            int idx = colorIndex(color);
            rowScore[idx][row] = scoreLine(buildLineStringRow(row, color));
            colScore[idx][col] = scoreLine(buildLineStringCol(col, color));
            String line1 = buildLineStringDiag1(diag1, color);
            diag1Score[idx][diag1] = line1.length() >= 5 ? scoreLine(line1) : 0;
            int k2 = row - col;
            String line2 = buildLineStringDiag2(k2, color);
            diag2Score[idx][diag2] = line2.length() >= 5 ? scoreLine(line2) : 0;
        }
    }

    private boolean isTimeUp() {
        if (!timeUp && System.currentTimeMillis() >= endTimeMs) {
            timeUp = true;
        }
        return timeUp;
    }

    // ==================== INNER CLASSES ====================

    private static class ThreatCount {
        int openFour = 0;
        int four = 0;
        int openThree = 0;

        boolean isDoubleThreat() {
            if (openFour >= 1) return true;
            if (four >= 2) return true;
            if (four >= 1 && openThree >= 1) return true;
            if (openThree >= 2) return true;
            return false;
        }
    }

    private static class TTEntry {
        static final int EXACT = 0;
        static final int LOWER = 1;
        static final int UPPER = -1;

        final int value;
        final int depth;
        final int flag;
        final int[] bestMove;

        TTEntry(int value, int depth, int flag, int[] bestMove) {
            this.value = value;
            this.depth = depth;
            this.flag = flag;
            this.bestMove = bestMove;
        }
    }

    private static class Move {
        final int r;
        final int c;
        final int score;

        Move(int r, int c, int score) {
            this.r = r;
            this.c = c;
            this.score = score;
        }
    }
}
