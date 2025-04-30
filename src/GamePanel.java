import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
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
        
        // Setup animation timer
        animationTimer = new Timer(16, e -> updateAnimations());
        animationTimer.start();
        
        // Setup removal timer
        removalTimer = new Timer(500, e -> { // 500ms delay
            if (!monstersToRemove.isEmpty()) {
                for (Monster monster : monstersToRemove) {
                    gameController.removeMonster(monster);
                }
                monstersToRemove.clear();
            }
        });
        removalTimer.setRepeats(false);
    }
    
    private void updateAnimations() {
        if (!gameController.isGameRunning()) return;
        
        // Update attack animation
        if (attackFrame > 0) {
            attackFrame--;
            if (attackFrame == 0 && targetMonster != null) {
                handleMonsterHit(targetMonster);
            }
        }
        
        // Update shake animation
        if (shakeFrame > 0) {
            shakeFrame--;
        }
        
        // Find target monster based on current input
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

        // Trigger hit animation
        monster.hit();
        
        // Start shake animation
        shakeFrame = SHAKE_DURATION;

        // Decrease monster health
        monster.decreaseHealth();

        // Check if monster should be removed
        if (monster.getHealth() <= 0) {
            if (canSplit) {
                // Add child monsters
                Monster[] children = monster.split();
                for (Monster child : children) {
                    gameController.getMonsters().add(child);
                }
            }

            // Apply monster powers
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
            
            // Mark monster for removal after delay
            monstersToRemove.add(monster);
            
            // Start removal timer
            removalTimer.restart();
            
            // Clear target monster and input
            targetMonster = null;
            gameController.getInputField().setText("");
        }
    }
    
    private Monster findTargetMonster() {
        ArrayList<Monster> monsters = gameController.getMonsters();
        if (monsters.isEmpty()) return null;
        
        // Find the monster that matches the current input
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
        
        int height = getHeight();
        int width = getWidth();

        // Draw base (left side)
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 0, height);
        
        // Draw shooter at left center with rotation
        if (SHOOTER_IMAGE != null) {
            int shooterSize = 60;
            int shooterX = 10;
            int shooterY = (height - shooterSize) / 2;
            
            // Calculate rotation angle if there's a target
            double angle = 0;
            if (targetMonster != null && !monstersToRemove.contains(targetMonster)) {
                int targetX = targetMonster.getX(width) + targetMonster.getSize() / 2;
                int targetY = targetMonster.getY(height) + targetMonster.getSize() / 2;
                int shooterCenterX = shooterX + shooterSize / 2;
                int shooterCenterY = shooterY + shooterSize / 2;
                
                angle = Math.atan2(targetY - shooterCenterY, targetX - shooterCenterX);
            }
            
            // Save the current transform
            AffineTransform oldTransform = g2d.getTransform();
            
            // Translate to the center of the shooter
            g2d.translate(shooterX + shooterSize/2, shooterY + shooterSize/2);
            
            // Rotate around the center
            g2d.rotate(angle);
            
            // Draw the shooter image centered
            g2d.drawImage(SHOOTER_IMAGE, -shooterSize/2, -shooterSize/2, shooterSize, shooterSize, null);
            
            // Draw attack effect if attacking
            if (attackFrame > 0 && targetMonster != null && !monstersToRemove.contains(targetMonster)) {
                float alpha = (float)attackFrame / MAX_ATTACK_FRAMES;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.YELLOW);
                
                // Draw beam from center to right side
                g2d.setStroke(new BasicStroke(5));
                g2d.drawLine(0, 0, shooterSize * 3, 0);
                
                // Draw muzzle flash
                g2d.fillOval(-shooterSize/4, -shooterSize/4, shooterSize/2, shooterSize/2);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            
            // Restore the original transform
            g2d.setTransform(oldTransform);
        }

        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            // Save the current transform
            AffineTransform monsterTransform = g2d.getTransform();
            
            // Apply shake effect to the monster being hit
            if (monster == targetMonster && shakeFrame > 0) {
                // Calculate shake offset based on time
                double shakeProgress = (double)shakeFrame / SHAKE_DURATION;
                int shakeOffset = (int)(10 * Math.sin(shakeProgress * Math.PI * 4)); // Faster shake
                g2d.translate(shakeOffset, 0);
            }

            monster.draw(g, width, height);
            
            // Restore the transform for the next monster
            g2d.setTransform(monsterTransform);
        }
    }
    
    // Public method to trigger an attack on a specific monster
    public void attackMonster(Monster monster) {
        if (monster != null) {
            targetMonster = monster;
            shootAtMonster(monster);
        }
    }
}