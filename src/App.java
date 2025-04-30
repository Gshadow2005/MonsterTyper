import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class App extends JFrame implements GameController.GameEventListener {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MainMenu mainMenu;
    private JPanel gameContainer;
    private GameController gameController;
    
    public App() {
        setTitle("Monster Typer");
        setSize(Constants.WIDTH, Constants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 700));
        setResizable(true);
        setLocationRelativeTo(null);
        
        // Create card layout for navigation
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create main menu
        mainMenu = new MainMenu(e -> startGame());
        
        // Create game container 
        gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(Color.BLACK);
        
        // Add panels to card layout
        mainPanel.add(mainMenu, "MENU");
        mainPanel.add(gameContainer, "GAME");
        
        // Start with menu
        cardLayout.show(mainPanel, "MENU");
        
        // Add main panel to frame
        add(mainPanel);

        boolean easyWordsLoaded = Constants.DIFFICULTY_WORDS.get(Constants.DIFFICULTY_EASY).length > 0;
        boolean mediumWordsLoaded = Constants.DIFFICULTY_WORDS.get(Constants.DIFFICULTY_MEDIUM).length > 0;
        boolean hardWordsLoaded = Constants.DIFFICULTY_WORDS.get(Constants.DIFFICULTY_HARD).length > 0;

        if (!easyWordsLoaded) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load easy words from file. Please check if 'assets/words/easy_words.txt' exists.", 
                "Error Loading Words", 
                JOptionPane.WARNING_MESSAGE);
        }
        
        if (!mediumWordsLoaded) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load medium words from file. Please check if 'assets/words/medium_words.txt' exists.", 
                "Error Loading Words", 
                JOptionPane.WARNING_MESSAGE);
        }
        
        if (!hardWordsLoaded) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load hard words from file. Please check if 'assets/words/hard_words.txt' exists.", 
                "Error Loading Words", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void startGame() {
        // First time setup of game components
        if (gameController == null) {
            gameController = new GameController();
            gameController.setGameEventListener(this);
            
            // Create and set up game panels
            GamePanel gamePanel = new GamePanel(gameController);
            gameContainer.add(gamePanel, BorderLayout.CENTER);
            
            // Create top info panel with custom layout
            JPanel topPanel = createTopPanel();
            gameContainer.add(topPanel, BorderLayout.NORTH);
            
            // Create input field panel
            JPanel inputPanel = createInputPanel();
            gameContainer.add(inputPanel, BorderLayout.SOUTH);
            
            // Make the input field resize properly when window is resized
            gameContainer.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // Calculate appropriate width based on current window size
                    // This ensures the input field stays proportional to the window
                    int windowWidth = gameContainer.getWidth();
                    int inputWidth = Math.min(400, windowWidth / 2);
                    
                    // Ensure minimum width
                    inputWidth = Math.max(inputWidth, 200);
                    
                    JTextField inputField = gameController.getInputField();
                    inputField.setPreferredSize(new Dimension(inputWidth, 50));
                    inputPanel.revalidate();
                }
            });
        } else {
            gameController.resetGame();
        }

        cardLayout.show(mainPanel, "GAME");
        gameController.getInputField().requestFocus();
        gameController.startGame();
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(new Color(20, 20, 30)); 
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Score display on left
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scorePanel.setOpaque(false);
        scorePanel.add(gameController.getScoreLabel());
        scorePanel.add(Box.createHorizontalStrut(20)); 
        scorePanel.add(gameController.getLivesLabel());
        
        // Style the score and lives labels
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        gameController.getScoreLabel().setFont(labelFont);
        gameController.getLivesLabel().setFont(labelFont);
        gameController.getScoreLabel().setForeground(new Color(255, 255, 100)); 
        gameController.getLivesLabel().setForeground(new Color(100, 255, 100)); 
        
        // Back button on right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton backButton = new JButton("Back to Menu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(50, 50, 120)); 
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(80, 80, 180)); 
                } else {
                    g2.setColor(new Color(60, 60, 150));
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                FontMetrics fm = g2.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
                
                int textX = (getWidth() - stringBounds.width) / 2;
                int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
                
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setPreferredSize(new Dimension(120, 35));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> returnToMenu());
        
        buttonPanel.add(backButton);
        
        topPanel.add(scorePanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false); 
        inputPanel.setBorder(new EmptyBorder(0, 0, 30, 0)); // bottom padding
        
        // Get and customize input field
        JTextField inputField = gameController.getInputField();
        styleInputField(inputField);

        JPanel indicatorPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (gameController != null) {
                    int indicatorSize = 10;
                    int indicatorX = 10;
                    int indicatorY = 10;
                    
                    // Keyboard jam indicator (red)
                    if (gameController.isKeyboardJammed()) {
                        // Draw main indicator
                        g2.setColor(new Color(255, 0, 0, 255));
                        g2.fillOval(indicatorX - 2, indicatorY - 4, indicatorSize, indicatorSize);
                    }
                    
                    // Input scramble indicator (blue)
                    if (gameController.isInputScrambled()) {
                        // Draw main indicator
                        g2.setColor(new Color(0, 0, 255, 255));
                        g2.fillOval(indicatorX - 2, indicatorY - 4, indicatorSize, indicatorSize);
                    }
                }
                
                g2.dispose();
            }
        };
        
        indicatorPanel.setOpaque(false);
        indicatorPanel.add(inputField, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        centerPanel.add(indicatorPanel);
        
        inputPanel.add(Box.createVerticalGlue());
        inputPanel.add(centerPanel);
        
        return inputPanel;
    }
    
    private void styleInputField(JTextField inputField) {
        Font inputFont = new Font("Consolas", Font.BOLD, 24);
        inputField.setFont(inputFont);
        inputField.setOpaque(false);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 255, 100), 2, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        inputField.setForeground(new Color(255, 255, 255));
        inputField.setCaretColor(Color.WHITE);
        inputField.setPreferredSize(new Dimension(400, 50));
        inputField.setMinimumSize(new Dimension(200, 50));
        inputField.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintBackground(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw base background
                g2.setColor(new Color(50, 50, 80, 80));
                g2.fillRoundRect(0, 0, inputField.getWidth(), inputField.getHeight(), 10, 10);
                
                g2.dispose();
            }
        });
    }
    
    private void returnToMenu() {
        if (gameController != null) {
            gameController.resetGame();
            gameController.stopGame(); 
        }
        
        // Switch to menu screen
        cardLayout.show(mainPanel, "MENU");
    }
    
    @Override
    public void onGameOver() {
        returnToMenu();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}