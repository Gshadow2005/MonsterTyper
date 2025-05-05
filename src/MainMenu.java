import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.sound.sampled.*;

public class MainMenu extends JPanel {
    private JButton playButton;
    private JButton exitButton;
    private ImageIcon logoGif;
    private Clip backgroundMusic;
    private boolean musicInitialized = false;

    private static final Color BACKGROUND_COLOR = new Color(10, 10, 20);
    private static final Color BUTTON_COLOR = new Color(80, 80, 200);
    private static final Color BUTTON_HOVER_COLOR = new Color(139, 0, 0);
    private static final Color BUTTON_PRESS_COLOR = new Color(200, 0, 0);
    
    public MainMenu(ActionListener playAction) {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR); 

        // Initialize the music but don't play it yet
        initializeBackgroundMusic();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BACKGROUND_COLOR);

        logoGif = loadLogoGif();
        JLabel logoLabel = new JLabel(logoGif);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel titleLabel = new JLabel("Monster Typer");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 42));
        titleLabel.setForeground(new Color(139, 0, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(150, 100));
        
        // Create buttons with click transition
        playButton = createMenuButton("Play Game");
        playButton.addActionListener(e -> {
            performButtonClickTransition(playButton, playAction);
        });
        
        exitButton = createMenuButton("Exit");
        exitButton.addActionListener(e -> {
            performButtonClickTransition(exitButton, evt -> {
                stopBackgroundMusic();
                System.exit(0);
            });
        });
        
        // Buttons to panel
        buttonPanel.add(playButton);
        buttonPanel.add(exitButton);
        
        // Components to center panel with spacing
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createVerticalGlue());
        
        // Center panel to main panel
        add(centerPanel, BorderLayout.CENTER);
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                playBackgroundMusic();
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                stopBackgroundMusic();
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    private void performButtonClickTransition(JButton button, ActionListener action) {
        Color originalColor = button.getBackground();
        Font originalFont = button.getFont();
        int originalFontSize = originalFont.getSize();
        
        button.setBackground(BUTTON_PRESS_COLOR);
        button.setFont(new Font(originalFont.getName(), originalFont.getStyle(), originalFontSize - 1));
        
        // Play click sound
        playClickSound();
        
        Timer transitionTimer = new Timer(150, e -> {
            Timer fadeTimer = new Timer(15, null);
            final int[] step = {0};
            final int totalSteps = 10;
            
            fadeTimer.addActionListener(evt -> {
                step[0]++;
                float ratio = (float) step[0] / totalSteps;
                
                if (step[0] >= totalSteps) {
                    fadeTimer.stop();
                    button.setFont(new Font(originalFont.getName(), originalFont.getStyle(), originalFontSize));
                    button.setBackground(originalColor);
                    action.actionPerformed(null);
                } else {
                    button.setBackground(interpolateColor(BUTTON_PRESS_COLOR, originalColor, ratio));
                    int newSize = Math.max(originalFontSize - 1, 
                                  originalFontSize - 1 + (int)(ratio * 1));
                    button.setFont(new Font(originalFont.getName(), originalFont.getStyle(), newSize));
                }
            });
            
            fadeTimer.start();
        });
        
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }
    
    private void playClickSound() {
        try {
            URL soundUrl = getClass().getResource("/assets/Sounds/click.wav");
            AudioInputStream audioStream;
            
            if (soundUrl != null) {
                audioStream = AudioSystem.getAudioInputStream(soundUrl);
            } else {
                File soundFile = new File("src/assets/Sounds/click.wav");
                if (!soundFile.exists()) {
                    return;
                }
                audioStream = AudioSystem.getAudioInputStream(soundFile);
            }
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            
        } catch (Exception e) {
        }
    }

    private void initializeBackgroundMusic() {
        try {
            URL audioUrl = getClass().getResource("/assets/Sounds/MainMenu.wav");
            AudioInputStream audioStream;
            
            if (audioUrl != null) {
                audioStream = AudioSystem.getAudioInputStream(audioUrl);
            } else {
                File audioFile = new File("src/assets/Sounds/MainMenu.wav");
                if (!audioFile.exists()) {
                    System.out.println("MainMenu.wav not found at: " + audioFile.getAbsolutePath());
                    return;
                }
                audioStream = AudioSystem.getAudioInputStream(audioFile);
            }
            
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            musicInitialized = true;
            
        } catch (Exception e) {
            System.out.println("Error initializing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playBackgroundMusic() {
        if (!musicInitialized) {
            initializeBackgroundMusic();
        }
        
        if (backgroundMusic != null && !backgroundMusic.isRunning()) {
            backgroundMusic.setFramePosition(0); // Reset to beginning
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        // Use a class constant for font size to ensure consistency
        final Font DEFAULT_BUTTON_FONT = new Font("Arial", Font.BOLD, 16);
        button.setFont(DEFAULT_BUTTON_FONT);
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
        });
        
        return button;
    }
    
    private Color interpolateColor(Color start, Color end, float ratio) {
        int r = Math.round(start.getRed() + ratio * (end.getRed() - start.getRed()));
        int g = Math.round(start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
        int b = Math.round(start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
        return new Color(r, g, b);
    }
    
    private ImageIcon loadLogoGif() {
        URL gifUrl = getClass().getResource("/assets/logo.gif");
        
        if (gifUrl != null) {
            return new ImageIcon(gifUrl);
        }
        
        try {
            File file = new File("assets/logo.gif");
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
        } catch (Exception e) {
        }

        System.out.println("logo.gif not found in assets folder");
        
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