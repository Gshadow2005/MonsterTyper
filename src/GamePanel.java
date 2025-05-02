import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
    private Image backgroundImage = null; 
    private Image cloudsImage = null; 
    private Image fortImage = null; // Image for FortniKoKoAndMarie
    private Monster targetMonster;
    private Timer animationTimer;
    private int attackFrame = 0;
    private static final int MAX_ATTACK_FRAMES = 10;
    private static final int SHAKE_DURATION = 20;
    private int shakeFrame = 0;
    
    // Explosion animation fields
    private ImageIcon explosionGif;
    private HashMap<Monster, ExplosionAnimation> explosions = new HashMap<>();
    
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
        
        // Try to load default background image
        try {
            ImageIcon bgIcon = new ImageIcon(GamePanel.class.getResource("/assets/BGniKoKoAndMarie_2.png"));
            if (bgIcon.getIconWidth() > 0) {
                backgroundImage = bgIcon.getImage();
            }
        } catch (Exception e) {
            System.out.println("No default background image found or error loading it: " + e.getMessage());
        }
        
        // Try to load clouds image
        try {
            ImageIcon cloudsIcon = new ImageIcon(GamePanel.class.getResource("/assets/CloudsniKoKoAndMarie_4.png"));
            if (cloudsIcon.getIconWidth() > 0) {
                cloudsImage = cloudsIcon.getImage();
            }
        } catch (Exception e) {
            System.out.println("No clouds image found or error loading it: " + e.getMessage());
        }
        
        // Try to load FortniKoKoAndMarie image
        try {
            ImageIcon fortIcon = new ImageIcon(GamePanel.class.getResource("/assets/FortniKoKoAndMarie.png"));
            if (fortIcon.getIconWidth() > 0) {
                fortImage = fortIcon.getImage();
            } else {
                System.out.println("Warning: FortniKoKoAndMarie image loaded but has invalid dimensions");
            }
        } catch (Exception e) {
            System.out.println("Failed to load FortniKoKoAndMarie image: " + e.getMessage());
        }
        
        // Try to load explosion GIF
        try {
            explosionGif = new ImageIcon(GamePanel.class.getResource("/assets/explosion.gif"));
            if (explosionGif.getIconWidth() <= 0) {
                System.out.println("Warning: Explosion GIF loaded but has invalid dimensions");
                explosionGif = null;
            }
        } catch (Exception e) {
            System.out.println("Failed to load explosion GIF: " + e.getMessage());
            explosionGif = null;
        }
        
        // Setup animation timer
        animationTimer = new Timer(16, e -> updateAnimations());
        animationTimer.start();
    }
    
    /**
     * Sets a new background image from the assets folder
     * @param filename The name of the image file in the assets folder (e.g., "background.gif")
     */
    public void setBackgroundImage(String filename) {
        try {
            ImageIcon icon = new ImageIcon(GamePanel.class.getResource("/assets/" + filename));
            if (icon.getIconWidth() > 0) {
                backgroundImage = icon.getImage();
                repaint();
            } else {
                System.out.println("Warning: Background image loaded but has invalid dimensions");
            }
        } catch (Exception e) {
            System.out.println("Failed to load background image: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    /**
     * Sets a new explosion GIF from the assets folder
     * @param filename The name of the GIF file in the assets folder (e.g., "explosion.gif")
     */
    public void setExplosionGif(String filename) {
        try {
            ImageIcon icon = new ImageIcon(GamePanel.class.getResource("/assets/" + filename));
            if (icon.getIconWidth() > 0) {
                explosionGif = icon;
                System.out.println("Successfully loaded explosion GIF: " + filename);
            } else {
                System.out.println("Warning: Explosion GIF loaded but has invalid dimensions");
            }
        } catch (Exception e) {
            System.out.println("Failed to load explosion GIF: " + e.getMessage());
            explosionGif = null;
        }
    }
    
    /**
     * Removes the background image (sets background to black)
     */
    public void clearBackgroundImage() {
        backgroundImage = null;
        repaint();
    }
    
    private void updateAnimations() {
        if (!gameController.isGameRunning()) return;
        
        if (attackFrame > 0) {
            attackFrame--;
            if (attackFrame == 0 && targetMonster != null) {
                handleMonsterHit(targetMonster);
            }
        }
        
        if (shakeFrame > 0) {
            shakeFrame--;
        }
        
        if (attackFrame == 0) {
            Monster newTarget = findTargetMonster();
            if (newTarget != null && newTarget != targetMonster) {
                targetMonster = newTarget;
                shootAtMonster(targetMonster);
            }
        }
        
        // Update explosion animations and remove completed ones
        Iterator<HashMap.Entry<Monster, ExplosionAnimation>> it = explosions.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<Monster, ExplosionAnimation> entry = it.next();
            ExplosionAnimation explosion = entry.getValue();
            explosion.update();
            
            if (explosion.isFinished()) {
                Monster monster = entry.getKey();
                // Remove monster from game when explosion finishes
                gameController.removeMonster(monster);
                it.remove();
            }
        }
        
        repaint();
    }
    
    private void handleMonsterHit(Monster monster) {
        boolean hasJamPower = monster.hasJamPower();
        boolean hasExtraLife = monster.hasExtraLife();
        boolean hasReverseInputPower = monster.hasReverseInputPower();
        boolean canSplit = monster.canSplit();

        monster.hit();
        shakeFrame = SHAKE_DURATION;
        monster.decreaseHealth();

        if (monster.getHealth() <= 0) {
            if (canSplit) {
                Monster[] children = monster.split();
                for (Monster child : children) {
                    gameController.addMonster(child);
                }
            }

            if (hasJamPower) {
                gameController.startKeyboardJam();
            }
            if (hasExtraLife) {
                gameController.increaseLife();
            }
            if (hasReverseInputPower) {
                gameController.startInputScramble();
            }

            gameController.increaseScore();
            
            // Add explosion animation at monster's position
            addExplosionAnimation(monster);
            
            // Clear the input field
            gameController.getInputField().setText("");
            
            // Reset target
            targetMonster = null;
        }
    }
    
    /**
     * Adds an explosion animation at the position of a defeated monster
     * @param monster The monster that was defeated
     */
    private void addExplosionAnimation(Monster monster) {
        if (explosionGif != null) {
            // Create explosion animation with monster reference
            ExplosionAnimation explosion = new ExplosionAnimation(monster);
            explosions.put(monster, explosion);
        }
    }
    
    private Monster findTargetMonster() {
        ArrayList<Monster> monsters = gameController.getMonsters();
        if (monsters.isEmpty()) return null;
        
        String currentInput = gameController.getInputField().getText().trim().toLowerCase();
        if (currentInput.isEmpty()) return null;
        
        for (Monster monster : monsters) {
            if (monster.getWord().toLowerCase().equals(currentInput)) {
                return monster;
            }
        }
        return null;
    }
    
    private void shootAtMonster(Monster monster) {
        targetMonster = monster;
        attackFrame = MAX_ATTACK_FRAMES;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // Draw the background image stretched to fit the panel
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, width, height, this);
        } else {
            // Fallback to black background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
        }

        // Draw FortniKoKoAndMarie image at the left side with better scaling
        if (fortImage != null) {
            int fortHeight = (int)(height * 0.75);
            int fortWidth = (int)(fortHeight * 0.25);
            
            // Cap the maximum width to 30% of the panel width to prevent over-expansion
            int maxFortWidth = (int)(width * 0.3);
            if (fortWidth > maxFortWidth) {
                fortWidth = maxFortWidth;
                fortHeight = (int)(fortWidth * 1.5); // Recalculate height if width was capped
            }
            
            // Position at left side, centered vertically
            int fortX = 0;
            int fortY = (height - fortHeight) / 2;

            g2d.drawImage(fortImage, fortX, fortY, fortWidth, fortHeight, this);
        }

        // Draw shooter with responsive size and x-offset
        if (SHOOTER_IMAGE != null) {
            int shooterSize = Math.min(width, height) / 15; // Shooter size is 1/15th of the smaller dimension
            int shooterX = (int) (width * 0.042); // Shooter x-offset is 4% of the panel width
            int shooterY = (height - shooterSize) / 2; // Centered vertically

            double angle = 0;
            if (targetMonster != null) {
                int targetX = targetMonster.getX(width) + targetMonster.getSize() / 2;
                int targetY = targetMonster.getY(height) + targetMonster.getSize() / 2;
                int shooterCenterX = shooterX + shooterSize / 2;
                int shooterCenterY = shooterY + shooterSize / 2;

                angle = Math.atan2(targetY - shooterCenterY, targetX - shooterCenterX);
            }

            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(shooterX + shooterSize / 2, shooterY + shooterSize / 2);
            g2d.rotate(angle);
            g2d.drawImage(SHOOTER_IMAGE, -shooterSize / 2, -shooterSize / 2, shooterSize, shooterSize, null);

            g2d.setTransform(oldTransform);
        }

        // Draw other elements (monsters, explosions, clouds, etc.)
        // Draw monsters - we draw normal monsters first, then monsters with explosions on top
        ArrayList<Monster> monsters = gameController.getMonsters();

        // First draw monsters that don't have explosions
        for (Monster monster : monsters) {
            if (!explosions.containsKey(monster)) {
                AffineTransform monsterTransform = g2d.getTransform();

                if (monster == targetMonster && shakeFrame > 0) {
                    double shakeProgress = (double) shakeFrame / SHAKE_DURATION;
                    int shakeOffset = (int) (10 * Math.sin(shakeProgress * Math.PI * 4));
                    g2d.translate(shakeOffset, 0);
                }

                monster.draw(g, width, height);
                g2d.setTransform(monsterTransform);
            }
        }

        // Now draw monsters with explosions and their explosions
        for (Monster monster : explosions.keySet()) {
            if (monsters.contains(monster)) {  // Only draw if monster is still in the game list
                // Don't draw the monster itself - it's being replaced by the explosion
                // Draw the explosion at the monster's current position
                explosions.get(monster).draw(g2d, width, height);
            }
        }

        // Draw clouds above the monsters
        if (cloudsImage != null) {
            int cloudsWidth = width;
            int cloudsHeight = height;
            int cloudsX = width - cloudsWidth;
            int cloudsY = 0;

            g2d.drawImage(cloudsImage, cloudsX, cloudsY, cloudsWidth, cloudsHeight, this);
        }
    }
    
    public void attackMonster(Monster monster) {
        if (monster != null) {
            targetMonster = monster;
            shootAtMonster(monster);
        }
    }
    
    /**
     * Inner class to handle the explosion animation
     */
    private class ExplosionAnimation {
        private Monster monster; // Store reference to the monster
        private long startTime;
        private int duration = 450; // Duration in milliseconds (1 second as specified)
        
        public ExplosionAnimation(Monster monster) {
            this.monster = monster;
            this.startTime = System.currentTimeMillis();
        }
        
        public void update() {
            // Nothing to update for GIF, it animates automatically
        }
        
        public boolean isFinished() {
            return System.currentTimeMillis() - startTime > duration;
        }
        
        public void draw(Graphics2D g, int panelWidth, int panelHeight) {
            if (explosionGif != null && monster != null) {
                // Get the monster's current position and size
                int monsterX = monster.getX(panelWidth);
                int monsterY = monster.getY(panelHeight);
                int monsterSize = monster.getSize();
                
                // Get the GIF dimensions
                int gifWidth = explosionGif.getIconWidth();
                int gifHeight = explosionGif.getIconHeight();
                
                // Calculate position to center the explosion over the monster
                int drawX = monsterX - (gifWidth - monsterSize) / 2;
                int drawY = monsterY - (gifHeight - monsterSize) / 2;
                
                // Draw the explosion GIF
                explosionGif.paintIcon(GamePanel.this, g, drawX, drawY);
            }
        }
    }
}