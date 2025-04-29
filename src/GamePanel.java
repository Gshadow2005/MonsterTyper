import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private GameController gameController;
    private BufferedImage backgroundImage;
    
    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(Color.BLACK); 
        gameController.setGamePanel(this);
        
        // Load the background image
        try {
            String imagePath = "src/assets/BlackHomies.png";
            File imageFile = new File(imagePath);
            
            if (imageFile.exists() && imageFile.isFile()) {
                backgroundImage = ImageIO.read(imageFile);
            } else {
                System.err.println("Background image file not found at: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // Draw background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);
        }
        
        // Draw base (left side)
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 5, panelHeight);
        
        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            monster.draw(g, panelWidth, panelHeight);
        }
    }
}