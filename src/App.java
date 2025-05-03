import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class App extends JFrame implements GameController.GameEventListener {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MainMenu mainMenu;
    private JPanel gameContainer;
    private GameController gameController;
    
    // Define button colors
    private static final Color BUTTON_HOVER_COLOR = new Color(139, 0, 0);
    private static final Color BUTTON_NORMAL_COLOR = new Color(60, 60, 150);
    private static final Color BUTTON_PRESSED_COLOR = new Color(50, 50, 120);
    
    // Animation properties
    private static final int ANIMATION_DURATION = 200; // milliseconds
    private static final int ANIMATION_STEPS = 10;
    
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

        class AnimatedButton extends JButton {
            private Color currentColor = BUTTON_NORMAL_COLOR;
            private Color targetColor = BUTTON_NORMAL_COLOR;
            private Timer animationTimer;
            private float animationProgress = 0f;
            
            public AnimatedButton(String text) {
                super(text);
                setupAnimation();
            }
            
            private void setupAnimation() {
                animationTimer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, e -> {
                    animationProgress += 1.0f / ANIMATION_STEPS;
                    
                    if (animationProgress >= 1.0f) {
                        animationProgress = 1.0f;
                        currentColor = targetColor;
                        ((Timer)e.getSource()).stop();
                    } else {
                        currentColor = interpolateColor(currentColor, targetColor, animationProgress);
                    }
                    
                    repaint();
                });

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        startColorTransition(BUTTON_HOVER_COLOR);
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        startColorTransition(BUTTON_NORMAL_COLOR);
                    }
                    
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        startColorTransition(BUTTON_PRESSED_COLOR);
                    }
                    
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        if (getModel().isRollover()) {
                            startColorTransition(BUTTON_HOVER_COLOR);
                        } else {
                            startColorTransition(BUTTON_NORMAL_COLOR);
                        }
                    }
                });
            }
            
            private void startColorTransition(Color newTargetColor) {
                targetColor = newTargetColor;
                animationProgress = 0f;
                animationTimer.restart();
            }
            
            private Color interpolateColor(Color c1, Color c2, float fraction) {
                int red = (int)(c1.getRed() + fraction * (c2.getRed() - c1.getRed()));
                int green = (int)(c1.getGreen() + fraction * (c2.getGreen() - c1.getGreen()));
                int blue = (int)(c1.getBlue() + fraction * (c2.getBlue() - c1.getBlue()));
                return new Color(red, green, blue);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(currentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                FontMetrics fm = g2.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
                
                int textX = (getWidth() - stringBounds.width) / 2;
                int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
                
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        }
        
        JButton backButton = new AnimatedButton("Back to Menu");
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

        // Create a custom panel that wraps the input field and draws indicators
        JPanel indicatorPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (gameController != null) {
                    int indicatorSize = 10;
                    // Position relative to the input field's border
                    int indicatorX = 5;  // Inside the border
                    int indicatorY = 5;  // Inside the border
                    
                    // Keyboard jam indicator (red)
                    if (gameController.isKeyboardJammed()) {
                        // Draw main indicator
                        g2.setColor(new Color(255, 0, 0, 255));
                        g2.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize);
                    }
                    
                    // Input scramble indicator (blue)
                    if (gameController.isInputScrambled()) {
                        // Draw main indicator
                        g2.setColor(new Color(0, 0, 255, 255));
                        g2.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize);
                    }
                }
                
                g2.dispose();
            }
        };
        
        indicatorPanel.setOpaque(false);
        indicatorPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); // Match input field border
        indicatorPanel.add(inputField, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        centerPanel.add(indicatorPanel);
        
        inputPanel.add(Box.createVerticalGlue());
        inputPanel.add(centerPanel);
        
        return inputPanel;
    }
    
    private void styleInputField(JTextField inputField) {
        Font inputFont = new Font("Consolas", Font.BOLD, 16);
        inputField.setFont(inputFont);
        inputField.setOpaque(false);
        
        // Create a custom border that changes color based on monster abilities
        inputField.setBorder(BorderFactory.createCompoundBorder(
            new Border() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (gameController != null) {
                        if (gameController.isKeyboardJammed()) {
                            g2.setColor(new Color(255, 0, 0, 200));
                        } else if (gameController.isInputScrambled()) {
                            g2.setColor(new Color(0, 0, 255, 200));
                        } else {
                            g2.setColor(new Color(100, 100, 255, 100));
                        }
                    } else {
                        g2.setColor(new Color(100, 100, 255, 100));
                    }
                    
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(x, y, width - 1, height - 1, 10, 10);
                    g2.dispose();
                }
                
                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(2, 2, 2, 2);
                }
                
                @Override
                public boolean isBorderOpaque() {
                    return false;
                }
            },
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