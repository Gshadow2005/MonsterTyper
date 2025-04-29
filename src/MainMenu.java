import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class MainMenu extends JPanel {
    private JButton playButton;
    private JButton exitButton;
    private ImageIcon logoGif;

    private static final Color BACKGROUND_COLOR = new Color(10, 10, 20);
    private static final Color BUTTON_COLOR = new Color(80, 80, 200);
    private static final Color BUTTON_HOVER_COLOR = new Color(139, 0, 0);
    
    public MainMenu(ActionListener playAction) {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR); 

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
        
        // Create buttons
        playButton = createMenuButton("Play Game");
        playButton.addActionListener(playAction);
        
        exitButton = createMenuButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        
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