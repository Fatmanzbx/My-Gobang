import java.awt.Dimension;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

/**
 * Main game window for Gobang.
 * Handles the chess board panel and game controls.
 */
public class Main extends JFrame {
    
    // Game mode constants
    public static final String MODE_PLAY_BLACK = "Play Black";
    public static final String MODE_PLAY_WHITE = "Play White";
    public static final String MODE_TWO_PLAYER = "Two Player";
    
    // Window dimensions
    private static final int GAME_WIDTH = 560;
    private static final int GAME_HEIGHT = 700;
    private static final int MENU_WIDTH = 280;
    private static final int MENU_HEIGHT = 350;
    
    // Save slots (3 slots) - saves folder is next to src folder
    private static final int MAX_SAVE_SLOTS = 3;
    private static final File SAVE_DIR = getSaveDirectory();
    
    private static File getSaveDirectory() {
        // Try to find saves folder relative to where the program runs
        File saves = new File("saves");
        if (!saves.exists()) {
            saves = new File("../saves"); // If running from src/
        }
        if (!saves.exists()) {
            saves = new File("WUZIQI/saves"); // If running from project root
        }
        return saves;
    }
    
    // UI Components
    private JButton undoButton = new JButton("Undo");
    private JButton saveButton = new JButton("Save");
    private JButton loadButton = new JButton("Load");
    private JButton menuButton = new JButton("Menu");
    private JButton prevButton = new JButton("<<<");
    private JButton nextButton = new JButton(">>>");
    
    private DrawChessBoard chessBoard;
    private String currentGameMode;

    /**
     * Constructor for viewing a recorded game.
     */
    public Main(Stone[] stones, String gameMode) {
        this.currentGameMode = gameMode;
        chessBoard = new DrawChessBoard(stones);
        setupReplayMode();
        setTitle("Gobang - Replay");
        add(chessBoard);
    }

    /**
     * Constructor for playing a new game.
     */
    public Main(String gameMode) {
        this.currentGameMode = gameMode;
        chessBoard = createChessBoard(gameMode);
        setupGameMode();
        if (MODE_TWO_PLAYER.equals(gameMode)) {
            setTitle("Gobang - " + gameMode);
        } else {
            setTitle("Gobang - " + gameMode + " (AI: " + AI.getDifficultyName() + ")");
        }
        add(chessBoard);
    }

    /**
     * Creates the appropriate chess board based on game mode.
     */
    private DrawChessBoard createChessBoard(String gameMode) {
        if (MODE_PLAY_BLACK.equals(gameMode)) {
            return new PlayBlack();
        } else if (MODE_PLAY_WHITE.equals(gameMode)) {
            return new PlayWhite();
        } else if (MODE_TWO_PLAYER.equals(gameMode)) {
            return new DoublePlayer();
        }
        return new DoublePlayer();
    }

    /**
     * Sets up the UI for replay mode.
     */
    private void setupReplayMode() {
        chessBoard.setLayout(null);
        
        chessBoard.add(menuButton);
        menuButton.setBounds(180, 30, 200, 30);
        
        chessBoard.add(prevButton);
        prevButton.setBounds(10, 300, 50, 100);
        
        chessBoard.add(nextButton);
        nextButton.setBounds(500, 300, 50, 100);
        
        menuButton.addActionListener(e -> openMainMenu());
        prevButton.addActionListener(e -> chessBoard.goBack());
        nextButton.addActionListener(e -> chessBoard.goForward());
    }

    /**
     * Sets up the UI for game mode.
     */
    private void setupGameMode() {
        Dimension buttonSize = new Dimension(200, 30);
        
        chessBoard.add(undoButton);
        undoButton.setPreferredSize(buttonSize);
        
        chessBoard.add(saveButton);
        saveButton.setPreferredSize(buttonSize);
        
        chessBoard.add(loadButton);
        loadButton.setPreferredSize(buttonSize);
        
        chessBoard.add(menuButton);
        menuButton.setPreferredSize(buttonSize);
        
        undoButton.addActionListener(e -> chessBoard.undoMove(2));
        saveButton.addActionListener(e -> showSaveDialog());
        loadButton.addActionListener(e -> showLoadDialog());
        menuButton.addActionListener(e -> openMainMenu());
    }
    
    /**
     * Gets the save file for a slot number.
     */
    private File getSaveFile(int slot) {
        return new File(SAVE_DIR, "save" + slot + ".txt");
    }
    
    /**
     * Gets info about a save slot.
     */
    private String getSlotInfo(int slot) {
        File file = getSaveFile(slot);
        if (!file.exists()) {
            return "Slot " + slot + ": Empty";
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String date = "Unknown";
            String mode = "Unknown";
            int moves = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# Saved:")) {
                    date = line.substring(9).trim();
                } else if (line.startsWith("# Mode:")) {
                    mode = line.substring(8).trim();
                } else if (line.startsWith("MOVES=")) {
                    moves = Integer.parseInt(line.substring(6));
                }
            }
            
            return "Slot " + slot + ": " + mode + " | " + moves + " moves | " + date;
        } catch (Exception e) {
            return "Slot " + slot + ": (corrupted)";
        }
    }
    
    /**
     * Shows the save dialog with 3 slots.
     */
    private void showSaveDialog() {
        if (chessBoard.getMoveCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "No moves to save!", 
                "Save Game", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Ensure save directory exists
        SAVE_DIR.mkdirs();
        
        // Build slot options
        String[] options = new String[MAX_SAVE_SLOTS + 1];
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            options[i - 1] = getSlotInfo(i);
        }
        options[MAX_SAVE_SLOTS] = "Cancel";
        
        int choice = JOptionPane.showOptionDialog(this,
            "Choose a slot to save:\n\n" + 
            options[0] + "\n" + options[1] + "\n" + options[2],
            "Save Game",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"Slot 1", "Slot 2", "Slot 3", "Cancel"},
            "Slot 1");
        
        if (choice >= 0 && choice < MAX_SAVE_SLOTS) {
            int slot = choice + 1;
            
            // Confirm overwrite if slot is not empty
            File file = getSaveFile(slot);
            if (file.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Slot " + slot + " already has a save.\nOverwrite?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            saveToSlot(slot);
        }
    }
    
    /**
     * Saves the game to a specific slot.
     */
    private void saveToSlot(int slot) {
        File file = getSaveFile(slot);
        
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("# Gobang Save File");
            writer.println("# Saved: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
            writer.println("# Mode: " + currentGameMode);
            writer.println("# Moves: " + chessBoard.getMoveCount());
            writer.println();
            writer.println("MODE=" + getModeCode(currentGameMode));
            writer.println("MOVES=" + chessBoard.getMoveCount());
            writer.println();
            
            for (int i = 1; i <= chessBoard.getMoveCount(); i++) {
                Stone stone = chessBoard.getStone(i);
                writer.println(stone.getColor() + "," + stone.getRow() + "," + stone.getCol());
            }
            
            JOptionPane.showMessageDialog(this, 
                "Game saved to Slot " + slot + "!", 
                "Save Game", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving game:\n" + e.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows the load dialog with 3 slots.
     */
    private void showLoadDialog() {
        // Ensure save directory exists
        SAVE_DIR.mkdirs();
        
        // Check if any saves exist
        boolean hasSaves = false;
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            if (getSaveFile(i).exists()) {
                hasSaves = true;
                break;
            }
        }
        
        if (!hasSaves) {
            JOptionPane.showMessageDialog(this, 
                "No saved games found!", 
                "Load Game", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Build slot info
        StringBuilder info = new StringBuilder("Choose a slot to load:\n\n");
        for (int i = 1; i <= MAX_SAVE_SLOTS; i++) {
            info.append(getSlotInfo(i)).append("\n");
        }
        
        int choice = JOptionPane.showOptionDialog(this,
            info.toString(),
            "Load Game",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"Slot 1", "Slot 2", "Slot 3", "Cancel"},
            "Slot 1");
        
        if (choice >= 0 && choice < MAX_SAVE_SLOTS) {
            int slot = choice + 1;
            File file = getSaveFile(slot);
            
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "Slot " + slot + " is empty!", 
                    "Load Game", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            loadFromSlot(slot);
        }
    }
    
    /**
     * Loads a game from a specific slot.
     */
    private void loadFromSlot(int slot) {
        File file = getSaveFile(slot);
        
        try {
            GameData gameData = loadGameFile(file);
            
            if (gameData == null || gameData.moveCount == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No valid moves found in save!", 
                    "Load Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Ask what to do
            String[] options = {"Replay", "Continue", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                "Loaded: " + gameData.moveCount + " moves (" + gameData.gameMode + ")\n\nWhat would you like to do?",
                "Load Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
            
            if (choice == 0) {
                // Replay mode
                Main replayWindow = new Main(gameData.stones, gameData.gameMode);
                replayWindow.setSize(GAME_WIDTH, GAME_HEIGHT);
                replayWindow.setLocationRelativeTo(null);
                replayWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                replayWindow.setVisible(true);
                dispose();
            } else if (choice == 1) {
                // Continue playing
                continuePlaying(gameData);
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading game:\n" + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads game data from a file.
     */
    private GameData loadGameFile(File file) throws IOException {
        GameData data = new GameData();
        data.stones = new Stone[226];
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int moveIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                if (line.startsWith("MODE=")) {
                    data.gameMode = getModeName(Integer.parseInt(line.substring(5)));
                } else if (line.startsWith("MOVES=")) {
                    data.moveCount = Integer.parseInt(line.substring(6));
                } else if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        moveIndex++;
                        data.stones[moveIndex] = new Stone(
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim())
                        );
                    }
                }
            }
            
            if (data.moveCount == 0) {
                data.moveCount = moveIndex;
            }
            if (data.gameMode == null) {
                data.gameMode = MODE_TWO_PLAYER;
            }
        }
        
        return data;
    }
    
    /**
     * Continues playing from loaded game state.
     */
    private void continuePlaying(GameData gameData) {
        Main gameWindow = new Main(gameData.gameMode);
        
        for (int i = 1; i <= gameData.moveCount; i++) {
            Stone stone = gameData.stones[i];
            if (stone != null) {
                gameWindow.chessBoard.restoreMove(stone);
            }
        }
        
        gameWindow.setSize(GAME_WIDTH, GAME_HEIGHT);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setVisible(true);
        dispose();
    }
    
    private int getModeCode(String modeName) {
        if (MODE_PLAY_BLACK.equals(modeName)) return -1;
        if (MODE_PLAY_WHITE.equals(modeName)) return 1;
        return 2;
    }
    
    private String getModeName(int code) {
        switch (code) {
            case -1: return MODE_PLAY_BLACK;
            case 1: return MODE_PLAY_WHITE;
            default: return MODE_TWO_PLAYER;
        }
    }

    /**
     * Opens the main menu.
     */
    private void openMainMenu() {
        Start menuFrame = new Start();
        menuFrame.setTitle("Gobang - Select Mode");
        menuFrame.setSize(MENU_WIDTH, MENU_HEIGHT);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setVisible(true);
        dispose();
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        Start menuFrame = new Start();
        menuFrame.setTitle("Gobang - Select Mode");
        menuFrame.setSize(MENU_WIDTH, MENU_HEIGHT);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setVisible(true);
    }
    
    private static class GameData {
        Stone[] stones;
        String gameMode;
        int moveCount;
    }
}
