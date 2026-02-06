# My-Gobang (Gobang / Renju)
Classic 15x15 Gobang with single-player (AI), two-player, save/load, and replay/analysis.

## How to Run
From the project root:
```bash
cd /Users/zbx/Desktop/My_Renju/WUZIQI
javac -d bin src/*.java
java -cp bin Main
```

## How to Play
- Black goes first. Players alternate placing stones on empty intersections.
- Win by forming an unbroken line of five stones horizontally, vertically, or diagonally.
- In single-player mode you can choose to play Black or White.
- Optional ban-hand (Renju) rules apply to Black:
  - Overline (more than five in a row) is not allowed for Black.
  - Double-three and double-four are not allowed for Black.
- Use the menu buttons to undo, save, load, or return to the main menu.

## Computer Player (AI) Summary
The AI is a deterministic game-search engine with tactical rules and an evaluation function.
It is designed to respond quickly while still finding strong moves.

Key components:
- **Iterative deepening**: searches depth 1..N, keeping the best move found so far so it can stop early
  when the time limit expires (limits depend on difficulty).
- **Alpha-beta pruning**: cuts off branches that cannot improve the current best score.
- **Transposition table**: caches evaluated positions using Zobrist hashing to avoid re-searching
  repeated states; stores best move, depth, and bounds.
- **Candidate move generation**: only considers empty points near existing stones, and scores
  candidates with a fast heuristic to explore the most promising moves first.
- **Tactical checks before full search**: immediate win, immediate block, open-four creation,
  and double-threat creation/blocking are detected quickly.
- **Pattern-based evaluation**: rows, columns, and diagonals are converted to line strings
  and scored using common Gomoku/Renju patterns (open four, four, open three, etc.).
- **Incremental evaluation**: line scores (rows/cols/diagonals) are cached and updated only on
  affected lines after each move, so the evaluation is fast inside the search.
- **Renju (ban-hand) rules for Black**: overline, double-three, and double-four are treated as
  forbidden moves for Black and are filtered out during search.

## Screenshots
### Menu
![image](https://github.com/Fatmanzbx/My-Gobang/blob/master/start.png)

### In Game
![image](https://github.com/Fatmanzbx/My-Gobang/blob/master/win.png)

### Analysis / Replay
![image](https://github.com/Fatmanzbx/My-Gobang/blob/master/reload.png)
