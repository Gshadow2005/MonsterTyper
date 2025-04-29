import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class MainMenu extends JPanel {
    private JButton playButton;
    private JButton exitButton;
    private ImageIcon logoGif;
    
    public MainMenu(ActionListener playAction) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 40)); // Dark blue background
        
        // Center panel for logo and buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(20, 20, 40));
        
        // Logo GIF - load from resources
        logoGif = loadLogoGif();
        JLabel logoLabel = new JLabel(logoGif);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("Monster Typer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(new Color(20, 20, 40));
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
        button.setBackground(new Color(80, 80, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 220), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setPreferredSize(new Dimension(150, 40));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 100, 220));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 200));
            }
        });
        
        return button;
    }
    
    private ImageIcon loadLogoGif() {
        // First try to load from assets folder as a resource
        URL gifUrl = getClass().getResource("/assets/logo.gif");
        
        if (gifUrl != null) {
            return new ImageIcon(gifUrl);
        }
        
        // If not found as a resource, try loading from file system
        try {
            File file = new File("assets/logo.gif");
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
        } catch (Exception e) {
            // Ignore this attempt
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
        
        // Timer for animation
        Timer animationTimer = new Timer(800, null);
        
        // Icon using first frame
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