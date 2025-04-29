import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Monster {
    private String word;
    private double relativeX, relativeY;

    // Safely load image using ImageIcon
    private static final Image MONSTER_IMAGE;

    static {
        ImageIcon icon = null;
        try {
            // Using absolute path with leading slash
            icon = new ImageIcon(Monster.class.getResource("/assets/MonsterTyper_Zombie.gif"));
            if (icon.getIconWidth() <= 0) {
                System.out.println("Warning: Image loaded but has invalid dimensions");
                icon = null;
            }
        } catch (Exception e) {
            System.out.println("Failed to load monster image: " + e.getMessage());
        }
        MONSTER_IMAGE = (icon != null) ? icon.getImage() : null;

        if (MONSTER_IMAGE == null) {
            System.out.println("Warning: MONSTER_IMAGE is null. Monster won't be drawn.");
        }
    }

    private boolean hasJamPower;
    private boolean hasExtraLife;

    /**
     * Constructor with three parameters, auto-assigns abilities based on chance
     */
    public Monster(int x, int y, String word) {
        // Initialize word first to avoid potential null reference
        this.word = word != null ? word : "";

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;

        // Auto-assign abilities based on chance
        this.hasJamPower = Constants.RANDOM.nextInt(100) < 20;     // 20% chance
        this.hasExtraLife = Constants.RANDOM.nextInt(100) < 10;    // 10% chance
    }

    /**
     * Constructor with four parameters, explicitly sets jam power
     */
    public Monster(int x, int y, String word, boolean hasJamPower) {
        // Initialize word first to avoid potential null reference
        this.word = word != null ? word : "";

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;

        // Explicitly set jam power
        this.hasJamPower = hasJamPower;
        
        // Auto-assign extra life based on chance
        this.hasExtraLife = Constants.RANDOM.nextInt(100) < 10;    // 10% chance
    }

    public void update(int panelWidth) {
        double pixelsToMove = Constants.currentMonsterSpeed;
        double moveAmount = pixelsToMove / Constants.WIDTH;
        relativeX -= moveAmount;
    }

    public void draw(Graphics g, int panelWidth, int panelHeight) {
        if (MONSTER_IMAGE == null) {
            // Draw a placeholder if image is not available
            drawPlaceholderMonster(g, panelWidth, panelHeight);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);
        int scaledSize = (int) (Constants.MONSTER_SIZE * Math.min(
            panelWidth / (double) Constants.WIDTH,
            panelHeight / (double) Constants.HEIGHT
        ));

        // Save the original transform
        AffineTransform oldTransform = g2d.getTransform();

        // Flip image horizontally
        g2d.translate(realX + scaledSize, realY);
        g2d.scale(-1, 1);
        g2d.drawImage(MONSTER_IMAGE, 0, 0, scaledSize, scaledSize, null);

        // Restore the original transform
        g2d.setTransform(oldTransform);

        // Draw health bar
        int healthBarHeight = 3;
        int healthBarWidth = scaledSize - 5;
        int healthBarX = realX;
        int healthBarY = realY - 10;

        g.setColor(Color.RED);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // Draw the word
        drawWord(g, realX, realY, scaledSize);
        
        // Draw power indicator if monster has special powers
        drawPowerIndicator(g, realX, realY, scaledSize);
    }
    
    private void drawPlaceholderMonster(Graphics g, int panelWidth, int panelHeight) {
        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);
        int scaledSize = (int) (Constants.MONSTER_SIZE * Math.min(
            panelWidth / (double) Constants.WIDTH,
            panelHeight / (double) Constants.HEIGHT
        ));
        
        // Draw a simple placeholder rectangle
        g.setColor(Color.GREEN);
        g.fillRect(realX, realY, scaledSize, scaledSize);
        
        // Draw the word
        drawWord(g, realX, realY, scaledSize);
        
        // Draw power indicator if monster has special powers
        drawPowerIndicator(g, realX, realY, scaledSize);
    }
    
    private void drawWord(Graphics g, int realX, int realY, int scaledSize) {
        if (word == null || word.isEmpty()) {
            return;
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(word);
        int textX = realX + (scaledSize - textWidth) / 2;
        int textY = realY + scaledSize + fm.getAscent();

        g.drawString(word, textX, textY);
    }
    
    private void drawPowerIndicator(Graphics g, int realX, int realY, int scaledSize) {
        // Draw jam power indicator
        if (hasJamPower) {
            g.setColor(Color.RED);
            g.fillOval(realX + scaledSize - 10, realY - 5, 8, 8);
        }
        
        // Draw extra life indicator
        if (hasExtraLife) {
            g.setColor(Color.GREEN);
            g.fillOval(realX + scaledSize - 20, realY - 5, 8, 8);
        }
    }

    public int getX(int panelWidth) {
        return (int) (relativeX * panelWidth);
    }

    public int getY(int panelHeight) {
        return (int) (relativeY * panelHeight);
    }

    public String getWord() {
        return word;
    }

    public boolean hasJamPower() {
        return hasJamPower;
    }

    public boolean hasExtraLife() {
        return hasExtraLife;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }
}