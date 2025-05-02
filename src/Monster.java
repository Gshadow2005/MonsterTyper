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
    private boolean hasReverseInputPower;
    private boolean canSplit;
    private boolean isChildMonster;
    private int health = 1;
    private int size;
    private int hitFlashFrame = 0;
    private static final int MAX_HIT_FLASH_FRAMES = 5;

    public Monster(int x, int y, String word) {
        // Initialize word first to avoid potential null reference
        this.word = word != null ? word : "";

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;
        this.size = Constants.MONSTER_SIZE;
        
        // Assign only one power to each monster
        int powerRoll = Constants.RANDOM.nextInt(100);
        
        // Split is super rare 
        if (powerRoll < Constants.SPLIT_CHANCE) {
            this.canSplit = true;
            this.hasJamPower = false;
            this.hasExtraLife = false;
            this.hasReverseInputPower = false;
            this.health = 2; //health of boss
            this.size = (int)(Constants.MONSTER_SIZE * 2);
        }
        // Jam power
        else if (powerRoll < Constants.SPLIT_CHANCE + Constants.JAM_POWER_CHANCE) {
            this.hasJamPower = true;
            this.canSplit = false;
            this.hasExtraLife = false;
            this.hasReverseInputPower = false;
        }
        // Reverse input power 
        else if (powerRoll < Constants.SPLIT_CHANCE + Constants.JAM_POWER_CHANCE + Constants.REVERSE_POWER_CHANCE) {
            this.hasReverseInputPower = true;
            this.hasJamPower = false;
            this.canSplit = false;
            this.hasExtraLife = false;
        }
        // Extra life
        else if (powerRoll < Constants.SPLIT_CHANCE + Constants.JAM_POWER_CHANCE + Constants.REVERSE_POWER_CHANCE + Constants.EXTRA_LIFE_CHANCE) {
            this.hasExtraLife = true;
            this.hasJamPower = false;
            this.canSplit = false;
            this.hasReverseInputPower = false;
        }
        // No power 
        else {
            this.hasJamPower = false;
            this.hasExtraLife = false;
            this.hasReverseInputPower = false;
            this.canSplit = false;
        }
        
        this.isChildMonster = false;
    }

    public Monster(int x, int y, String word, boolean hasJamPower) {
        this.word = word != null ? word : "";

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;
        this.size = Constants.MONSTER_SIZE;
        this.hasJamPower = hasJamPower;
        this.hasExtraLife = false;
        this.hasReverseInputPower = false;
        this.canSplit = false;
        
        this.isChildMonster = false;
    }
    
    public Monster(double relX, double relY, String word, int size, boolean isChild) {
        this.word = word != null ? word : "";
        this.relativeX = relX;
        this.relativeY = relY;
        this.size = size;
        
        // Child monsters don't have special powers
        this.hasJamPower = false;
        this.hasExtraLife = false;
        this.hasReverseInputPower = false;
        this.canSplit = false;
        this.health = 1;
        this.isChildMonster = isChild;
    }

    public void update(int panelWidth) {
        double pixelsToMove = Constants.currentMonsterSpeed;

        if (isChildMonster) {
            pixelsToMove *= 1.2;
        }
        double moveAmount = pixelsToMove / Constants.WIDTH;
        relativeX -= moveAmount;
        
        // Update hit flash animation
        if (hitFlashFrame > 0) {
            hitFlashFrame--;
        }
    }

    public void draw(Graphics g, int panelWidth, int panelHeight) {
        if (MONSTER_IMAGE == null) {
            drawPlaceholderMonster(g, panelWidth, panelHeight);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);
        int scaledSize = (int) (size * Math.min(
            panelWidth / (double) Constants.WIDTH,
            panelHeight / (double) Constants.HEIGHT
        ));

        AffineTransform oldTransform = g2d.getTransform();

        // Apply hit flash effect
        if (hitFlashFrame > 0) {
            // Add a slight "bounce" effect when hit
            int bounceOffset = (int)(3 * Math.sin(hitFlashFrame * Math.PI / MAX_HIT_FLASH_FRAMES));
            g2d.translate(0, bounceOffset);
            
            // Flash overlay
            float alpha = (float)hitFlashFrame / MAX_HIT_FLASH_FRAMES;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(Color.WHITE);
            g2d.fillRect(realX - 5, realY - 5, scaledSize + 10, scaledSize + 10);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Flip image horizontally
        g2d.translate(realX + scaledSize, realY);
        g2d.scale(-1, 1);
        g2d.drawImage(MONSTER_IMAGE, 0, 0, scaledSize, scaledSize, null);

        // Restore the original transform
        g2d.setTransform(oldTransform);

        // Draw health bar
        drawHealthBar(g, realX, realY, scaledSize);

        // Draw the word
        drawWord(g, realX, realY, scaledSize);
        
        // Draw power indicator if monster has special powers
        drawPowerIndicator(g, realX, realY, scaledSize);
    }
    
    private void drawHealthBar(Graphics g, int realX, int realY, int scaledSize) {
        int healthBarHeight = 3;
        int healthBarWidth = scaledSize - 5;
        int healthBarX = realX;
        int healthBarY = realY - 10;

        // Background (empty) health bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        
        // Filled health based on current health
        float healthPercent = health / (canSplit ? 2.0f : 1.0f);
        int filledWidth = (int)(healthBarWidth * healthPercent);
        
        // Flash effect when hit
        if (hitFlashFrame > 0) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.RED);
        }
        g.fillRect(healthBarX, healthBarY, filledWidth, healthBarHeight);
    }
    
    private void drawPlaceholderMonster(Graphics g, int panelWidth, int panelHeight) {
        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);
        int scaledSize = (int) (size * Math.min(
            panelWidth / (double) Constants.WIDTH,
            panelHeight / (double) Constants.HEIGHT
        ));
        
        // Apply hit animation for placeholder monsters too
        if (hitFlashFrame > 0) {
            // Draw a white halo/glow effect
            g.setColor(new Color(255, 255, 255, 50 + hitFlashFrame * 20));
            g.fillOval(
                realX - 5, 
                realY - 5, 
                scaledSize + 10, 
                scaledSize + 10
            );
        }
        
        // Draw a simple placeholder rectangle
        if (isChildMonster) {
            g.setColor(new Color(100, 180, 100)); // Lighter green for children
        } else {
            g.setColor(Color.GREEN);
        }
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
        
        // Highlight word if being hit
        if (hitFlashFrame > 0) {
            // Use a highlighted color during flash
            g.setColor(new Color(255, 255, 0)); // Yellow highlight
            
            // Scale effect during hit
            int fontScale = isChildMonster ? 
                10 + (int)(hitFlashFrame * 0.6) : 
                12 + (int)(hitFlashFrame * 0.8);
            g.setFont(new Font("Arial", Font.BOLD, fontScale));
        } else {
            // Normal color
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, isChildMonster ? 12 : 14));
        }
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(word);
        int textX = realX + (scaledSize - textWidth) / 2;
        int textY = realY + scaledSize + fm.getAscent();

        g.drawString(word, textX, textY);
    }
    
    private void drawPowerIndicator(Graphics g, int realX, int realY, int scaledSize) {
        int indicatorY = realY - 5;
        int indicatorSize = 8;
        
        // Draw only one power indicator per monster
        if (hasJamPower) {
            g.setColor(Color.RED);
            g.fillOval(realX + scaledSize - 20, indicatorY, indicatorSize, indicatorSize);
        } else if (hasExtraLife) {
            g.setColor(Color.GREEN);
            g.fillOval(realX + scaledSize - 20, indicatorY, indicatorSize, indicatorSize);
        } else if (hasReverseInputPower) {
            g.setColor(Color.BLUE);
            g.fillOval(realX + scaledSize - 20, indicatorY, indicatorSize, indicatorSize);
        } else if (canSplit) {
            g.setColor(Color.YELLOW);
            g.fillOval(realX + scaledSize - 20, indicatorY, indicatorSize, indicatorSize);
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
    
    public boolean hasReverseInputPower() {
        return hasReverseInputPower;
    }
    
    public boolean canSplit() {
        return canSplit;
    }
    
    public boolean isChildMonster() {
        return isChildMonster;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void decreaseHealth() {
        health--;
    }
    
    public int getSize() {
        return size;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }
    
    public Monster[] split() {
        if (!canSplit) {
            return new Monster[0];
        }

        int childSize = (int)(size * 0.5); // 50%
        String childWord = word.length() > 3 ? word.substring(0, word.length() / 2) : word;

        int childCount = 7; 
        //int childCount = Constants.RANDOM.nextInt(2) + 2; // random 3 to 2 monsterheheh
        Monster[] children = new Monster[childCount];
        
        for (int i = 0; i < childCount; i++) {
            double offsetX = relativeX + (Constants.RANDOM.nextDouble() * 0.05 - 0.025);
            double offsetY = relativeY + (Constants.RANDOM.nextDouble() * 0.05 - 0.025);

            String monsterWord = (childWord.length() > i) 
                ? childWord.substring(i, i+1) 
                : Character.toString('a' + Constants.RANDOM.nextInt(26));
                
            children[i] = new Monster(offsetX, offsetY, monsterWord, childSize, true);
        }
        
        return children;
    }

    public void hit() {
        hitFlashFrame = MAX_HIT_FLASH_FRAMES;
    }
}