import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * Start menu for Gobang game.
 * Allows player to select game mode and AI difficulty.
 */
public class Start extends JFrame {
    
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_X = 70;
    
    private JSlider difficultySlider;
    
    public Start() {
        // Create mode selection buttons
        JButton playBlackButton = new JButton("Play Black");
        playBlackButton.setBounds(BUTTON_X, 130, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        JButton playWhiteButton = new JButton("Play White");
        playWhiteButton.setBounds(BUTTON_X, 170, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        JButton twoPlayerButton = new JButton("2 Player");
        twoPlayerButton.setBounds(BUTTON_X, 210, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        // Create difficulty label
        JLabel difficultyLabel = new JLabel("AI Difficulty:");
        difficultyLabel.setBounds(BUTTON_X, 255, 140, 20);
        difficultyLabel.setForeground(Color.WHITE);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Create difficulty slider (0=Easy, 1=Medium, 2=Hard)
        difficultySlider = new JSlider(JSlider.HORIZONTAL, 0, 2, 1);
        difficultySlider.setBounds(BUTTON_X - 10, 275, 160, 45);
        difficultySlider.setMajorTickSpacing(1);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setPaintLabels(true);
        difficultySlider.setSnapToTicks(true);
        difficultySlider.setOpaque(false);
        difficultySlider.setForeground(Color.WHITE);
        
        // Custom labels for slider
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        JLabel easyLabel = new JLabel("Easy");
        easyLabel.setForeground(Color.WHITE);
        easyLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        JLabel mediumLabel = new JLabel("Medium");
        mediumLabel.setForeground(Color.WHITE);
        mediumLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        JLabel hardLabel = new JLabel("Hard");
        hardLabel.setForeground(Color.WHITE);
        hardLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        labels.put(0, easyLabel);
        labels.put(1, mediumLabel);
        labels.put(2, hardLabel);
        difficultySlider.setLabelTable(labels);
        
        // Set up background panel
        BackgroundPanel panel = new BackgroundPanel();
        panel.add(playBlackButton);
        panel.add(playWhiteButton);
        panel.add(twoPlayerButton);
        panel.add(difficultyLabel);
        panel.add(difficultySlider);
        add(panel);
        
        // Button actions
        playBlackButton.addActionListener(e -> startGame(Main.MODE_PLAY_BLACK));
        playWhiteButton.addActionListener(e -> startGame(Main.MODE_PLAY_WHITE));
        twoPlayerButton.addActionListener(e -> startGame(Main.MODE_TWO_PLAYER));
    }

    /**
     * Starts a new game with the specified mode.
     * @param mode The game mode to start
     */
    private void startGame(String mode) {
        // Set AI difficulty before starting game
        AI.setDifficulty(difficultySlider.getValue());
        
        Main gameWindow = new Main(mode);
        gameWindow.setSize(560, 700);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setVisible(true);
        dispose();
    }
}

/**
 * Custom panel with background image.
 */
class BackgroundPanel extends JPanel {
    
    private final Image backgroundImage;
    
    public BackgroundPanel() {
        backgroundImage = Toolkit.getDefaultToolkit().getImage(
            BackgroundPanel.class.getResource("/qipanstart.jpg")
        );
        setOpaque(true);
        setLayout(null);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
