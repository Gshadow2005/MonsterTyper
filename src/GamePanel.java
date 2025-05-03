import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
    
    // Laser beam animation properties
    private static final Image[] LASER_BEAM_FRAMES; 
    private boolean isShootingAnimation = false; 
    private int currentLaserFrame = 0; 
    private int laserAnimationSpeed = 1; 
    private int frameCounter = 0; 
    private Image backgroundImage = null; 
    private Image cloudsImage = null; 
    private Monster targetMonster;
    private Timer animationTimer;
    private int attackFrame = 0;
    private static final int MAX_ATTACK_FRAMES = 10;
    private static final int SHAKE_DURATION = 20;
    private int shakeFrame = 0;
    private boolean shouldCenterShooter = false;
    private long shooterCenterDelayMillis = 1000; 
    private long shooterCenterTimeTarget = 0;
    
    // Explosion animation fields
    private static final Image[] EXPLOSION_FRAMES;
    private HashMap<Monster, ExplosionAnimation> explosions = new HashMap<>();
    private int explosionAnimationSpeed = 3; // Default explosion animation speed
    
    static {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(GamePanel.class.getResource("/assets/Shooter.gif"));
            if (icon.getIconWidth() <= 0) {
                System.out.println("Warning: Shooter image loaded but has invalid dimensions");
                icon = null;
            }
        } catch (Exception e) {
            System.out.println("Failed to load shooter image: " + e.getMessage());
        }
        SHOOTER_IMAGE = (icon != null) ? icon.getImage() : null;
        
        LASER_BEAM_FRAMES = new Image[14];
        for (int i = 0; i < 14; i++) {
            try {
                String path = "/assets/Laser/Laser_Beam" + (i + 1) + ".png";
                ImageIcon laserIcon = new ImageIcon(GamePanel.class.getResource(path));
                if (laserIcon.getIconWidth() <= 0) {
                    System.out.println("Warning: Laser Beam frame " + (i + 1) + " loaded but has invalid dimensions");
                    LASER_BEAM_FRAMES[i] = null;
                } else {
                    LASER_BEAM_FRAMES[i] = laserIcon.getImage();
                }
            } catch (Exception e) {
                System.out.println("Failed to load Laser Beam frame " + (i + 1) + ": " + e.getMessage());
                LASER_BEAM_FRAMES[i] = null;
            }
        }
        
        // Load explosion frames
        EXPLOSION_FRAMES = new Image[12];
        for (int i = 0; i < 12; i++) {
            try {
                String path = "/assets/Explosion/Explosion" + (i + 1) + ".png";
                ImageIcon explosionIcon = new ImageIcon(GamePanel.class.getResource(path));
                if (explosionIcon.getIconWidth() <= 0) {
                    System.out.println("Warning: Explosion frame " + (i + 1) + " loaded but has invalid dimensions");
                    EXPLOSION_FRAMES[i] = null;
                } else {
                    EXPLOSION_FRAMES[i] = explosionIcon.getImage();
                }
            } catch (Exception e) {
                System.out.println("Failed to load Explosion frame " + (i + 1) + ": " + e.getMessage());
                EXPLOSION_FRAMES[i] = null;
            }
        }
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
    
        // Setup animation timer
        animationTimer = new Timer(16, e -> updateAnimations());
        animationTimer.start();
        
        // Set initial laser size
        setLaserSize(100, 60);
        
        // Set initial explosion size
        setExplosionSize(200, 200);
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
    
    public void setLaserAnimationSpeed(int speed) {
        if (speed < 1) speed = 1;
        this.laserAnimationSpeed = speed;
    }

    public void setExplosionAnimationSpeed(int speed) {
        if (speed < 1) speed = 1;
        this.explosionAnimationSpeed = speed;
        System.out.println("Explosion animation speed set to: " + speed);
    }

    /**
     * Sets the delay in milliseconds before the shooter centers after firing
     * @param millis The delay in milliseconds
     */
    public void setShooterCenterDelay(long millis) {
        if (millis < 0) millis = 0;
        this.shooterCenterDelayMillis = millis;
        System.out.println("Shooter centering delay set to: " + millis + "ms");
    }

    public void setLaserSize(int width, int height) {
        for (int i = 0; i < 14; i++) {
            try {
                String path = "/assets/Laser/Laser_Beam" + (i + 1) + ".png";
                ImageIcon originalIcon = new ImageIcon(GamePanel.class.getResource(path));
                if (originalIcon.getIconWidth() > 0) {
                    Image originalImage = originalIcon.getImage();
                    Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    LASER_BEAM_FRAMES[i] = resizedImage;
                }
            } catch (Exception e) {
                System.out.println("Failed to resize Laser Beam frame " + (i + 1) + ": " + e.getMessage());
            }
        }
        repaint();
    }
    
    public void setExplosionSize(int width, int height) {
        for (int i = 0; i < 12; i++) {
            try {
                String path = "/assets/Explosion/Explosion" + (i + 1) + ".png";
                ImageIcon originalIcon = new ImageIcon(GamePanel.class.getResource(path));
                if (originalIcon.getIconWidth() > 0) {
                    Image originalImage = originalIcon.getImage();
                    Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    EXPLOSION_FRAMES[i] = resizedImage;
                }
            } catch (Exception e) {
                System.out.println("Failed to resize Explosion frame " + (i + 1) + ": " + e.getMessage());
            }
        }
        repaint();
    }
    
    public void clearBackgroundImage() {
        backgroundImage = null;
        repaint();
    }
    
    private void updateAnimations() {
        if (!gameController.isGameRunning()) return;

        if (shouldCenterShooter && System.currentTimeMillis() >= shooterCenterTimeTarget) {
            targetMonster = null;
            shouldCenterShooter = false;
            repaint();
        }
        
        if (isShootingAnimation) {
            frameCounter++;

            if (frameCounter >= laserAnimationSpeed) {
                frameCounter = 0;
                currentLaserFrame++;

                if (currentLaserFrame >= LASER_BEAM_FRAMES.length) {
                    if (attackFrame > 0) {
                        currentLaserFrame = 0;
                    } else {
                        isShootingAnimation = false;
                        
                        // When shooting animation ends, schedule shooter centering
                        shouldCenterShooter = true;
                        shooterCenterTimeTarget = System.currentTimeMillis() + shooterCenterDelayMillis;
                    }
                }
            }
        }
        
        if (attackFrame > 0) {
            attackFrame--;
            if (attackFrame == 0) {
                if (targetMonster != null) {
                    handleMonsterHit(targetMonster);
                }
            }
        }
        
        if (shakeFrame > 0) {
            shakeFrame--;
        }
        
        if (attackFrame == 0 && !isShootingAnimation && !shouldCenterShooter) {
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
        }
    }
    
    private void addExplosionAnimation(Monster monster) {
        ExplosionAnimation explosion = new ExplosionAnimation(monster);
        explosions.put(monster, explosion);
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
        isShootingAnimation = true; 
        currentLaserFrame = 0; 
        frameCounter = 0; 
        shouldCenterShooter = false; 
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
            int shooterX = (int) (width * 0.042); // Shooter x-offset is 4% of the panel width
            int shooterY = (height - shooterSize) / 2; // Centered vertically

            double angle = 0;
            int targetX = 0;
            int targetY = 0;
            int shooterCenterX = shooterX + shooterSize / 2;
            int shooterCenterY = shooterY + shooterSize / 2;
            
            if (targetMonster != null) {
                targetX = targetMonster.getX(width) + targetMonster.getSize() / 2;
                targetY = targetMonster.getY(height) + targetMonster.getSize() / 2;
                
                angle = Math.atan2(targetY - shooterCenterY, targetX - shooterCenterX);
            }

            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(shooterCenterX, shooterCenterY);
            g2d.rotate(angle);
            
            // Draw the shooter
            g2d.drawImage(SHOOTER_IMAGE, -shooterSize / 2, -shooterSize / 2, shooterSize, shooterSize, null);
            if (isShootingAnimation && currentLaserFrame < LASER_BEAM_FRAMES.length && LASER_BEAM_FRAMES[currentLaserFrame] != null) {
                Image currentLaserImage = LASER_BEAM_FRAMES[currentLaserFrame];

                // Position the laser beam beside the shooter
                int laserX = shooterSize / 3; // Right side of the shooter
                int laserY = -currentLaserImage.getHeight(this) / 2; // Center vertically

                g2d.drawImage(currentLaserImage, laserX, laserY, this);
            }

            g2d.setTransform(oldTransform);
        }

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

        // Draw monsters with explosions
        for (Monster monster : explosions.keySet()) {
            if (monsters.contains(monster)) {  
                AffineTransform monsterTransform = g2d.getTransform();
                monster.draw(g, width, height);
                g2d.setTransform(monsterTransform);
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
     * Inner class to handle the frame-by-frame explosion animation
     */
    private class ExplosionAnimation {
        private Monster monster; // Store reference to the monster
        private int currentFrame = 0;
        private int frameCounter = 0;
        
        public ExplosionAnimation(Monster monster) {
            this.monster = monster;
        }
        
        public void update() {
            frameCounter++;
            
            if (frameCounter >= explosionAnimationSpeed) {
                frameCounter = 0;
                currentFrame++;
            }
        }
        
        public boolean isFinished() {
            return currentFrame >= EXPLOSION_FRAMES.length;
        }
        
        public void draw(Graphics2D g, int panelWidth, int panelHeight) {
            if (currentFrame < EXPLOSION_FRAMES.length && EXPLOSION_FRAMES[currentFrame] != null && monster != null) {
                // Get the monster's current position and size
                int monsterX = monster.getX(panelWidth);
                int monsterY = monster.getY(panelHeight);
                int monsterSize = monster.getSize();
                
                // Get the explosion frame dimensions
                Image currentExplosionImage = EXPLOSION_FRAMES[currentFrame];
                int explosionWidth = currentExplosionImage.getWidth(null);
                int explosionHeight = currentExplosionImage.getHeight(null);
                
                if (explosionWidth <= 0 || explosionHeight <= 0) {
                    // Use estimated size if actual dimensions are not available
                    explosionWidth = 120;
                    explosionHeight = 120;
                }
                
                // Calculate position to center the explosion over the monster
                int drawX = monsterX + (monsterSize - explosionWidth) / 2;
                int drawY = monsterY + (monsterSize - explosionHeight) / 2;
                
                // Draw the explosion frame
                g.drawImage(currentExplosionImage, drawX, drawY, null);
            }
        }
    }
}