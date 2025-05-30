import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.sound.sampled.*;
import java.io.File;

public class GameOver extends JPanel {
    private JButton playAgainButton;
    private JButton exitButton;
    private ImageIcon logoGif;
    private int finalScore;

    private static final Color BACKGROUND_COLOR = new Color(10, 10, 20);
    private static final Color BUTTON_COLOR = new Color(80, 80, 200);
    private static final Color BUTTON_HOVER_COLOR = new Color(139, 0, 0);
    
    // Sound-related fields
    private final String GAME_OVER_SOUND = "src/assets/Sounds/GameOverSound.wav";
    private Clip gameOverSoundClip;
    private boolean isSoundPlaying = false;

    public GameOver(ActionListener playAgainAction, ActionListener exitAction, int score) {
        this.finalScore = score;
        
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR); 

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BACKGROUND_COLOR);

        // Load logo gif from MainMenu
        logoGif = loadLogoGif();
        JLabel logoLabel = new JLabel(logoGif);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Game Over title
        JLabel gameOverLabel = new JLabel("");
        gameOverLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        gameOverLabel.setForeground(new Color(139, 0, 0));
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Score display
        JLabel scoreLabel = new JLabel("Your Score: " + finalScore);
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Button panel - changed to horizontal layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create buttons with the same styling as MainMenu
        playAgainButton = createMenuButton("Play Again");
        playAgainButton.addActionListener(playAgainAction);
        
        exitButton = createMenuButton("Main Menu");
        exitButton.addActionListener(exitAction);
        
        // Add buttons to panel
        buttonPanel.add(playAgainButton);
        buttonPanel.add(exitButton);
        
        // Components to center panel with spacing
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(gameOverLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(scoreLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createVerticalGlue());
        
        // Center panel to main panel
        add(centerPanel, BorderLayout.CENTER);
        
        // Initialize and play the game over sound
        initSounds();
        playGameOverSound();
    }
    
    /**
     * Initialize sound effects
     */
    private void initSounds() {
        try {
            File gameOverSoundFile = new File(GAME_OVER_SOUND);
            if (gameOverSoundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(gameOverSoundFile);
                gameOverSoundClip = AudioSystem.getClip();
                gameOverSoundClip.open(audioStream);
                
                // Add a listener to detect when sound finishes playing
                gameOverSoundClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        isSoundPlaying = false;
                    }
                });
            } else {
                System.out.println("Game over sound file not found: " + GAME_OVER_SOUND);
            }
        } catch (Exception e) {
            System.out.println("Error initializing game over sound: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Play the game over sound effect
     */
    public void playGameOverSound() {
        if (gameOverSoundClip != null && !isSoundPlaying) {
            gameOverSoundClip.setFramePosition(0);
            gameOverSoundClip.start();
            isSoundPlaying = true;
        }
    }
    
    /**
     * Stop the game over sound if it's playing
     */
    public void stopGameOverSound() {
        if (gameOverSoundClip != null && isSoundPlaying) {
            gameOverSoundClip.stop();
            isSoundPlaying = false;
        }
    }
    
    /**
     * Cleanup resources before panel is disposed
     */
    public void cleanup() {
        stopGameOverSound();
        if (gameOverSoundClip != null) {
            gameOverSoundClip.close();
        }
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 30, 30), 2), 
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        button.setPreferredSize(new Dimension(150, 40));

        final String originalText = text;
        final Timer[] glitchTextTimer = {null};
        final Timer[] glitchColorTimer = {null};

        button.addMouseListener(new MouseAdapter() {
            private Timer hoverTimer;
            private Timer exitTimer;
            private final int ANIMATION_DURATION = 200; 
            private final int ANIMATION_STEPS = 10;
            
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (exitTimer != null && exitTimer.isRunning()) {
                    exitTimer.stop();
                }
                
                if (glitchTextTimer[0] != null) {
                    glitchTextTimer[0].stop();
                }
                
                if (glitchColorTimer[0] != null) {
                    glitchColorTimer[0].stop();
                }

                // Start color transition animation
                final Color startColor = button.getBackground();
                final Color targetColor = BUTTON_HOVER_COLOR;
                
                hoverTimer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
                final int[] step = {0};
                
                hoverTimer.addActionListener(e -> {
                    step[0]++;
                    if (step[0] <= ANIMATION_STEPS) {
                        float ratio = (float) step[0] / ANIMATION_STEPS;
                        Color newColor = interpolateColor(startColor, targetColor, ratio);
                        button.setBackground(newColor);
                    } else {
                        button.setBackground(targetColor);
                        hoverTimer.stop();
                        
                        // Start glitch effects after color transition completes
                        startGlitchEffects(button, originalText);
                    }
                });
                
                hoverTimer.start();
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                if (glitchTextTimer[0] != null) {
                    glitchTextTimer[0].stop();
                }
                if (glitchColorTimer[0] != null) {
                    glitchColorTimer[0].stop();
                }
                button.setText(originalText);
                
                if (hoverTimer != null && hoverTimer.isRunning()) {
                    hoverTimer.stop();
                }

                final Color startColor = button.getBackground();
                final Color targetColor = BUTTON_COLOR;
                
                exitTimer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
                final int[] step = {0};
                
                exitTimer.addActionListener(e -> {
                    step[0]++;
                    if (step[0] <= ANIMATION_STEPS) {
                        float ratio = (float) step[0] / ANIMATION_STEPS;
                        Color newColor = interpolateColor(startColor, targetColor, ratio);
                        button.setBackground(newColor);
                    } else {
                        button.setBackground(targetColor);
                        exitTimer.stop();
                    }
                });
                
                exitTimer.start();
            }

            private void startGlitchEffects(JButton button, String originalText) {
                glitchTextTimer[0] = new Timer(100, null);
                glitchTextTimer[0].addActionListener(e -> {
                    if (Math.random() < 0.4) {
                        button.setText(createGlitchedText(originalText));
                    } else {
                        button.setText(originalText);
                    }
                });
                glitchTextTimer[0].start();
                
                glitchColorTimer[0] = new Timer(150, null);
                glitchColorTimer[0].addActionListener(e -> {
                    if (Math.random() < 0.10) {
                        Color glitchColor = new Color(
                            Math.min(255, BUTTON_HOVER_COLOR.getRed() + 50),
                            BUTTON_HOVER_COLOR.getGreen(),
                            BUTTON_HOVER_COLOR.getBlue()
                        );
                        button.setBackground(glitchColor);

                        Timer resetTimer = new Timer(50, event -> {
                            button.setBackground(BUTTON_HOVER_COLOR);
                        });
                        resetTimer.setRepeats(false);
                        resetTimer.start();
                    }
                });
                glitchColorTimer[0].start();
            }

            private String createGlitchedText(String originalText) {
                StringBuilder glitched = new StringBuilder(originalText);
                int numGlitches = 1 + (int)(Math.random() * 2);
                
                for (int i = 0; i < numGlitches; i++) {
                    if (glitched.length() == 0) break;
                    
                    int pos = (int)(Math.random() * glitched.length());
                    char glitchChar;

                    String glitchChars = "!@#$%oten^&*<>|/\\";
                    if (Math.random() < 0.9) {
                        glitchChar = glitchChars.charAt((int)(Math.random() * glitchChars.length()));
                    } else {
                        char original = glitched.charAt(pos);
                        glitchChar = (char)(original + (-5 + (int)(Math.random() * 10)));
                    }
                    
                    glitched.setCharAt(pos, glitchChar);
                }
                return glitched.toString();
            }
            
            private Color interpolateColor(Color start, Color end, float ratio) {
                int r = Math.round(start.getRed() + ratio * (end.getRed() - start.getRed()));
                int g = Math.round(start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
                int b = Math.round(start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
                return new Color(r, g, b);
            }
        });
        
        return button;
    }
    
    private ImageIcon loadLogoGif() {
        URL gifUrl = getClass().getResource("/assets/GameOver.gif");
        
        if (gifUrl != null) {
            return new ImageIcon(gifUrl);
        }
        
        try {
            java.io.File file = new java.io.File("assets/GameOver.gif");
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Error loading logo: " + e.getMessage());
        }

        System.out.println("gameover.gif not found in assets folder");

        return createPlaceholderLogo();
    }
    
    private ImageIcon createPlaceholderLogo() {
        int width = 200;
        int height = 120;

        BufferedImage[] frames = new BufferedImage[2];
        
        for (int i = 0; i < 2; i++) {
            frames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            
            // Fill background
            g.setColor(new Color(40, 40, 100));
            g.fillRect(0, 0, width, height);
            
            // Draw border
            g.setColor(new Color(60, 60, 160));
            g.setStroke(new BasicStroke(4));
            g.drawRect(2, 2, width - 4, height - 4);
            
            g.dispose();
        }

        Timer animationTimer = new Timer(800, null);

        ImageIcon icon = new ImageIcon(frames[0]);
        
        // Animation behavior
        final int[] currentFrame = {0};
        animationTimer.addActionListener(e -> {
            currentFrame[0] = (currentFrame[0] + 1) % frames.length;
            icon.setImage(frames[currentFrame[0]]);
        });
        
        animationTimer.start();
        
        return icon;
    }
}