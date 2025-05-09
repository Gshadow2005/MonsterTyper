import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Monster {
    private String word;
    private String secondWord; 
    private String thirdWord;
    private double relativeX, relativeY;

    private static final Image MONSTER_IMAGE;
    private static final Image CHILD_MONSTER_IMAGE;
    private static final Image BOSS_MONSTER_IMAGE;
    private static final Image JAM_MONSTER_IMAGE;
    private static final Image LIFE_MONSTER_IMAGE;
    private static final Image REVERSE_MONSTER_IMAGE;
    
    // List to store medium words
    private static final List<String> MEDIUM_WORDS = new ArrayList<>();
    private static final String MEDIUM_WORDS_FILE = "/assets/words/medium_words.txt"; 

    static {
        ImageIcon monsterIcon = null;
        ImageIcon childMonsterIcon = null;
        ImageIcon bossMonsterIcon = null;
        ImageIcon jamMonsterIcon = null;
        ImageIcon lifeMonsterIcon = null;
        ImageIcon reverseMonsterIcon = null;
        try {
            // Using absolute path with leading slash
            monsterIcon = new ImageIcon(Monster.class.getResource("/assets/MonsterTyper_Zombie.gif"));
            if (monsterIcon.getIconWidth() <= 0) {
                System.out.println("Warning: Monster image loaded but has invalid dimensions");
                monsterIcon = null;
            }
            
            // Load the child monster image
            childMonsterIcon = new ImageIcon(Monster.class.getResource("/assets/bat.gif"));
            if (childMonsterIcon.getIconWidth() <= 0) {
                System.out.println("Warning: Child monster image loaded but has invalid dimensions");
                childMonsterIcon = null;
            }
            
            // Load the boss monster image
            bossMonsterIcon = new ImageIcon(Monster.class.getResource("/assets/MonsterTyper_Boss.gif"));
            if (bossMonsterIcon.getIconWidth() <= 0) {
                System.out.println("Warning: Boss monster image loaded but has invalid dimensions");
                bossMonsterIcon = null;
            }
            
            // Load the jam monster image
            jamMonsterIcon = new ImageIcon(Monster.class.getResource("/assets/MonsterTyper_JamMonster.gif"));
            if (jamMonsterIcon.getIconWidth() <= 0) {
                System.out.println("Warning: Jam monster image loaded but has invalid dimensions");
                jamMonsterIcon = null;
            }
            
            // Load the extra life monster image
            lifeMonsterIcon = new ImageIcon(Monster.class.getResource("/assets/MonsterTyper_Life.gif"));
            if (lifeMonsterIcon.getIconWidth() <= 0) {
                System.out.println("Warning: Life monster image loaded but has invalid dimensions");
                lifeMonsterIcon = null;
            }
            
            // Load the reverse monster image
            reverseMonsterIcon = new ImageIcon(Monster.class.getResource("/assets/MonsterTyper_Reverse.gif"));
            if (reverseMonsterIcon.getIconWidth() <= 0) {
                System.out.println("Warning: Reverse monster image loaded but has invalid dimensions");
                reverseMonsterIcon = null;
            }
        } catch (Exception e) {
            System.out.println("Failed to load monster images: " + e.getMessage());
        }
        MONSTER_IMAGE = (monsterIcon != null) ? monsterIcon.getImage() : null;
        CHILD_MONSTER_IMAGE = (childMonsterIcon != null) ? childMonsterIcon.getImage() : null;
        BOSS_MONSTER_IMAGE = (bossMonsterIcon != null) ? bossMonsterIcon.getImage() : null;
        JAM_MONSTER_IMAGE = (jamMonsterIcon != null) ? jamMonsterIcon.getImage() : null;
        LIFE_MONSTER_IMAGE = (lifeMonsterIcon != null) ? lifeMonsterIcon.getImage() : null;
        REVERSE_MONSTER_IMAGE = (reverseMonsterIcon != null) ? reverseMonsterIcon.getImage() : null;

        if (MONSTER_IMAGE == null) {
            System.out.println("Warning: MONSTER_IMAGE is null. Monster won't be drawn.");
        }
        
        if (CHILD_MONSTER_IMAGE == null) {
            System.out.println("Warning: CHILD_MONSTER_IMAGE is null. Child monsters will use placeholder.");
        }
        
        if (BOSS_MONSTER_IMAGE == null) {
            System.out.println("Warning: BOSS_MONSTER_IMAGE is null. Boss monsters will use placeholder.");
        }
        
        if (JAM_MONSTER_IMAGE == null) {
            System.out.println("Warning: JAM_MONSTER_IMAGE is null. Jam monsters will use placeholder.");
        }
        
        if (LIFE_MONSTER_IMAGE == null) {
            System.out.println("Warning: LIFE_MONSTER_IMAGE is null. Life monsters will use placeholder.");
        }
        
        if (REVERSE_MONSTER_IMAGE == null) {
            System.out.println("Warning: REVERSE_MONSTER_IMAGE is null. Reverse monsters will use placeholder.");
        }
        
        // Load medium words
        loadMediumWords();
    }
    
    // Method to load medium words for child monsters
    private static void loadMediumWords() {
        try {
            InputStream is = Monster.class.getResourceAsStream(MEDIUM_WORDS_FILE);
            if (is == null) {
                System.out.println("Warning: Could not find medium words file: " + MEDIUM_WORDS_FILE);
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    MEDIUM_WORDS.add(line.trim());
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error loading medium words: " + e.getMessage());
        }
    }
    
    // Get a random medium word
    private static String getRandomMediumWord() {
        if (MEDIUM_WORDS.isEmpty()) {
            return "medium"; // Fallback word
        }
        return MEDIUM_WORDS.get(Constants.RANDOM.nextInt(MEDIUM_WORDS.size()));
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
            this.health = 3; // Health of boss
            this.size = (int)(Constants.MONSTER_SIZE * 3.5);
            
            // Generate a second word for the boss monster
            this.secondWord = getRandomMediumWord();
            this.thirdWord = getRandomMediumWord();
        }
        // Jam power
        else if (powerRoll < Constants.SPLIT_CHANCE + Constants.JAM_POWER_CHANCE) {
            this.hasJamPower = true;
            this.canSplit = false;
            this.hasExtraLife = false;
            this.hasReverseInputPower = false;
            this.size = (int)(Constants.MONSTER_SIZE * 1); 
        }
        // Reverse input power 
        else if (powerRoll < Constants.SPLIT_CHANCE + Constants.JAM_POWER_CHANCE + Constants.REVERSE_POWER_CHANCE) {
            this.hasReverseInputPower = true;
            this.hasJamPower = false;
            this.canSplit = false;
            this.hasExtraLife = false;
            this.size = (int)(Constants.MONSTER_SIZE * 1.3); 
        }
        // Extra life
        else if (powerRoll < Constants.SPLIT_CHANCE + Constants.JAM_POWER_CHANCE + Constants.REVERSE_POWER_CHANCE + Constants.EXTRA_LIFE_CHANCE) {
            this.hasExtraLife = true;
            this.hasJamPower = false;
            this.canSplit = false;
            this.hasReverseInputPower = false;
            this.size = (int)(Constants.MONSTER_SIZE * 0.8);
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
            pixelsToMove *= 2; // Child monsters move faster
        } else if (hasJamPower) {
            pixelsToMove *= 1.9; // Jam monsters move faster
        } else if (hasReverseInputPower) {
            pixelsToMove *= 1.5; // Reverse monsters speed
        } else if (hasExtraLife) {
            pixelsToMove *= 1; // Life monsters normal speed
        }
        
        double moveAmount = pixelsToMove / Constants.WIDTH;
        relativeX -= moveAmount;
        
        // Update hit flash animation
        if (hitFlashFrame > 0) {
            hitFlashFrame--;
        }
    }

    public void draw(Graphics g, int panelWidth, int panelHeight) {
        Image imageToUse;
        if (isChildMonster) {
            imageToUse = CHILD_MONSTER_IMAGE;
        } else if (canSplit) {
            imageToUse = BOSS_MONSTER_IMAGE;
        } else if (hasJamPower) {
            imageToUse = JAM_MONSTER_IMAGE;
        } else if (hasExtraLife) {
            imageToUse = LIFE_MONSTER_IMAGE;
        } else if (hasReverseInputPower) {
            imageToUse = REVERSE_MONSTER_IMAGE;
        } else {
            imageToUse = MONSTER_IMAGE;
        }
        
        if (imageToUse == null) {
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
        
        if (hasJamPower && !isChildMonster) {
            scaledSize = (int)(scaledSize * 1.3);
        } else if (hasExtraLife && !isChildMonster) {
            scaledSize = (int)(scaledSize * 1.2); 
        } else if (hasReverseInputPower && !isChildMonster) {
            scaledSize = (int)(scaledSize * 1.2);
        }

        AffineTransform oldTransform = g2d.getTransform();

        // Apply hit flash effect
        if (hitFlashFrame > 0) {
            // Add a slight "bounce" effect when hit
            int bounceOffset = (int)(3 * Math.sin(hitFlashFrame * Math.PI / MAX_HIT_FLASH_FRAMES));
            g2d.translate(0, bounceOffset);
        }

        // Flip image horizontally
        g2d.translate(realX + scaledSize, realY);
        g2d.scale(-1, 1);

        if (hasJamPower && !isChildMonster) {
            int yOffset = 0; 
            g2d.drawImage(imageToUse, 0, yOffset, scaledSize, scaledSize, null);
        } else if (hasExtraLife && !isChildMonster) {
            int yOffset = 0;
            g2d.drawImage(imageToUse, 0, yOffset, scaledSize, scaledSize, null);
        } else if (hasReverseInputPower && !isChildMonster) {
            int yOffset = 0;
            g2d.drawImage(imageToUse, 0, yOffset, scaledSize, scaledSize, null);
        } else {
            g2d.drawImage(imageToUse, 0, 0, scaledSize, scaledSize, null);
        }

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
        float healthPercent = health / (canSplit ? 3.0f : 1.0f);
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

        if (isChildMonster) {
            g.setColor(new Color(100, 180, 100)); // Lighter green for children
        } else if (canSplit) {
            g.setColor(new Color(200, 130, 30)); // Orange for boss monsters
        } else if (hasJamPower) {
            g.setColor(new Color(255, 0, 255)); // Purple for jam monsters
        } else if (hasExtraLife) {
            g.setColor(new Color(0, 220, 0)); // Green for life monsters
        } else if (hasReverseInputPower) {
            g.setColor(new Color(30, 144, 255)); // Blue for reverse monsters
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
    
        if (canSplit) {
            if (health == 2 && secondWord != null) {
                word = secondWord;
            } else if (health == 1 && thirdWord != null) {
                word = thirdWord;
            }
        }
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
    
        int childSize = (int)(size * 0.3); 
        
        int childCount = 5; // children count when splitting
        Monster[] children = new Monster[childCount];
        
        for (int i = 0; i < childCount; i++) {
            double spreadFactor = 0.15; 

            double angle = (1 * Math.PI * i) / childCount; 
            double distance = spreadFactor * (0.5 + 0.3 * Constants.RANDOM.nextDouble()); 
            
            double offsetX = relativeX + (distance * Math.cos(angle));
            double offsetY = relativeY + (distance * Math.sin(angle));

            offsetX = Math.max(0.05, Math.min(0.95, offsetX));
            offsetY = Math.max(0.05, Math.min(0.95, offsetY));
            
            // Use medium words for child monsters
            String childWord = getRandomMediumWord();
                
            children[i] = new Monster(offsetX, offsetY, childWord, childSize, true);
        }
        
        return children;
    }

    public void hit() {
        hitFlashFrame = MAX_HIT_FLASH_FRAMES;
    }
}