import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
    private Image backgroundImage = null; 
    private Image cloudsImage = null; 
    private Monster targetMonster;
    private Timer animationTimer;
    private Timer removalTimer;
    private int attackFrame = 0;
    private static final int MAX_ATTACK_FRAMES = 10;
    private static final int SHAKE_DURATION = 20;
    private int shakeFrame = 0;
    private ArrayList<Monster> monstersToRemove = new ArrayList<>();
    
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
            ImageIcon bgIcon = new ImageIcon(GamePanel.class.getResource("/assets/BGniKoKoAndMarie.png"));
            if (bgIcon.getIconWidth() > 0) {
                backgroundImage = bgIcon.getImage();
            }
        } catch (Exception e) {
            System.out.println("No default background image found or error loading it: " + e.getMessage());
        }
        
        // Try to load clouds image
        try {
            ImageIcon cloudsIcon = new ImageIcon(GamePanel.class.getResource("/assets/CloudsniKoKoAndMarie.png"));
            if (cloudsIcon.getIconWidth() > 0) {
                cloudsImage = cloudsIcon.getImage();
            }
        } catch (Exception e) {
            System.out.println("No clouds image found or error loading it: " + e.getMessage());
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
        
        // Setup removal timer
        removalTimer = new Timer(500, e -> {
            if (!monstersToRemove.isEmpty()) {
                for (Monster monster : monstersToRemove) {
                    gameController.removeMonster(monster);
                }
                monstersToRemove.clear();
            }
        });
        removalTimer.setRepeats(false);
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
        ArrayList<Monster> finishedExplosions = new ArrayList<>();
        for (Monster monster : explosions.keySet()) {
            ExplosionAnimation explosion = explosions.get(monster);
            explosion.update();
            if (explosion.isFinished()) {
                finishedExplosions.add(monster);
            }
        }
        
        for (Monster monster : finishedExplosions) {
            explosions.remove(monster);
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
                    gameController.getMonsters().add(child);
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
            
            monstersToRemove.add(monster);
            removalTimer.restart();
            targetMonster = null;
            gameController.getInputField().setText("");
        }
    }
    
    /**
     * Adds an explosion animation at the position of a defeated monster
     * @param monster The monster that was defeated
     */
    private void addExplosionAnimation(Monster monster) {
        if (explosionGif != null) {
            int width = getWidth();
            int height = getHeight();
            
            // Get the monster's position and size
            int monsterX = monster.getX(width);
            int monsterY = monster.getY(height);
            int monsterSize = monster.getSize();
            
            // Store a direct reference to the monster for better positioning
            ExplosionAnimation explosion = new ExplosionAnimation(monsterX, monsterY, monsterSize, monsterSize);
            explosion.monster = monster; // Set monster reference directly
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

        // Draw shooter with responsive size and x-offset
        if (SHOOTER_IMAGE != null) {
            int shooterSize = Math.min(width, height) / 15; // Shooter size is 1/15th of the smaller dimension
            int shooterX = (int) (width * 0.035); // Shooter x-offset is 5% of the panel width
            int shooterY = (height - shooterSize) / 2; // Centered vertically

            double angle = 0;
            if (targetMonster != null && !monstersToRemove.contains(targetMonster)) {
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

            if (attackFrame > 0 && targetMonster != null && !monstersToRemove.contains(targetMonster)) {
                float alpha = (float) attackFrame / MAX_ATTACK_FRAMES;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(5));
                g2d.drawLine(0, 0, shooterSize * 3, 0);
                g2d.fillOval(-shooterSize / 4, -shooterSize / 4, shooterSize / 2, shooterSize / 2);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            g2d.setTransform(oldTransform);
        }

        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            AffineTransform monsterTransform = g2d.getTransform();

            if (monster == targetMonster && shakeFrame > 0) {
                double shakeProgress = (double) shakeFrame / SHAKE_DURATION;
                int shakeOffset = (int) (10 * Math.sin(shakeProgress * Math.PI * 4));
                g2d.translate(shakeOffset, 0);
            }

            monster.draw(g, width, height);
            g2d.setTransform(monsterTransform);
        }
        
        // Draw explosion animations
        if (explosionGif != null) {
            for (Monster monster : explosions.keySet()) {
                ExplosionAnimation explosion = explosions.get(monster);
                explosion.draw(g2d);
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
        private int x, y;
        private Monster monster; // Store reference to the monster
        private int duration = 1000; // Duration in milliseconds (adjust as needed)
        private long startTime;
        
        public ExplosionAnimation(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            for (Monster m : monstersToRemove) {
                if (m.getX(getWidth()) == x && m.getY(getHeight()) == y) {
                    this.monster = m;
                    break;
                }
            }
            this.startTime = System.currentTimeMillis();
        }
        
        public void update() {
            // Nothing to update for GIF, it animates automatically
        }
        
        public boolean isFinished() {
            return System.currentTimeMillis() - startTime > duration;
        }
        
        public void draw(Graphics2D g) {
            if (explosionGif != null) {
                // Get the GIF dimensions
                int gifWidth = explosionGif.getIconWidth();
                int gifHeight = explosionGif.getIconHeight();
                
                // Calculate position to center the explosion over the monster
                int drawX = x;
                int drawY = y;
                
                if (monster != null) {
                    // If we have the monster reference, center explosion over it
                    int monsterSize = monster.getSize();
                    drawX = x - (gifWidth - monsterSize) / 2;
                    drawY = y - (gifHeight - monsterSize) / 2;
                }
                
                // Draw the explosion GIF
                explosionGif.paintIcon(GamePanel.this, g, drawX, drawY);
            }
        }
    }
}