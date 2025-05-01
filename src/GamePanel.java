import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
    private Image backgroundImage = null; // Background image instance
    private Monster targetMonster;
    private Timer animationTimer;
    private Timer removalTimer;
    private int attackFrame = 0;
    private static final int MAX_ATTACK_FRAMES = 10;
    private static final int SHAKE_DURATION = 20;
    private int shakeFrame = 0;
    private ArrayList<Monster> monstersToRemove = new ArrayList<>();
    
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
            monstersToRemove.add(monster);
            removalTimer.restart();
            targetMonster = null;
            gameController.getInputField().setText("");
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

        // Draw semi-transparent black overlay on left side
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 80, height);
        
        // Draw shooter
        if (SHOOTER_IMAGE != null) {
            int shooterSize = 60;
            int shooterX = 10;
            int shooterY = (height - shooterSize) / 2;
            
            double angle = 0;
            if (targetMonster != null && !monstersToRemove.contains(targetMonster)) {
                int targetX = targetMonster.getX(width) + targetMonster.getSize() / 2;
                int targetY = targetMonster.getY(height) + targetMonster.getSize() / 2;
                int shooterCenterX = shooterX + shooterSize / 2;
                int shooterCenterY = shooterY + shooterSize / 2;
                
                angle = Math.atan2(targetY - shooterCenterY, targetX - shooterCenterX);
            }
            
            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(shooterX + shooterSize/2, shooterY + shooterSize/2);
            g2d.rotate(angle);
            g2d.drawImage(SHOOTER_IMAGE, -shooterSize/2, -shooterSize/2, shooterSize, shooterSize, null);
            
            if (attackFrame > 0 && targetMonster != null && !monstersToRemove.contains(targetMonster)) {
                float alpha = (float)attackFrame / MAX_ATTACK_FRAMES;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(5));
                g2d.drawLine(0, 0, shooterSize * 3, 0);
                g2d.fillOval(-shooterSize/4, -shooterSize/4, shooterSize/2, shooterSize/2);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            
            g2d.setTransform(oldTransform);
        }

        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            AffineTransform monsterTransform = g2d.getTransform();
            
            if (monster == targetMonster && shakeFrame > 0) {
                double shakeProgress = (double)shakeFrame / SHAKE_DURATION;
                int shakeOffset = (int)(10 * Math.sin(shakeProgress * Math.PI * 4));
                g2d.translate(shakeOffset, 0);
            }

            monster.draw(g, width, height);
            g2d.setTransform(monsterTransform);
        }
    }
    
    public void attackMonster(Monster monster) {
        if (monster != null) {
            targetMonster = monster;
            shootAtMonster(monster);
        }
    }
}