import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
    
    static {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(GamePanel.class.getResource("/assets/MonsterTyper_Shooter.png"));
            if (icon.getIconWidth() <= 0) {
                System.out.println("Warning: Shooter image loaded but has invalid dimensions");
                icon = null;
            }
        } catch (Exception e) {
            System.out.println("Failed to load shooter image: " + e.getMessage());
        }
        SHOOTER_IMAGE = (icon != null) ? icon.getImage() : null;
    }
    
    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(Color.BLACK);
        gameController.setGamePanel(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int height = getHeight();
        int width = getWidth();

        // Draw base (left side)
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 5, height);
        
        // Draw shooter at left center
        if (SHOOTER_IMAGE != null) {
            int shooterSize = 60; // Adjust size as needed
            int shooterX = 10; // Position from left edge
            int shooterY = (height - shooterSize) / 2; // Center vertically
            g.drawImage(SHOOTER_IMAGE, shooterX, shooterY, shooterSize, shooterSize, null);
        }

        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            monster.draw(g, width, height);
        }
    }
}