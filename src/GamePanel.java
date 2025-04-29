import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private GameController gameController;
    private static final Image SHOOTER_IMAGE;
    private List<Projectile> projectiles;
    private Monster targetMonster;
    private Timer projectileTimer;
    
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
        
        // Initialize projectiles list
        projectiles = new ArrayList<>();
        
        // Setup projectile timer
        projectileTimer = new Timer(16, e -> updateProjectiles());
        projectileTimer.start();
    }
    
    private void updateProjectiles() {
        if (!gameController.isGameRunning()) return;
        
        // Update existing projectiles and check for collisions
        projectiles.removeIf(projectile -> {
            projectile.update();
            
            // Check if projectile reached its target
            if (projectile.hasReachedTarget()) {
                return true;
            }
            
            // Check for collisions with monsters
            ArrayList<Monster> monsters = gameController.getMonsters();
            for (Monster monster : monsters) {
                if (projectile.intersects(monster, getWidth(), getHeight())) {
                    // Handle monster hit
                    handleMonsterHit(monster);
                    return true; // Remove the projectile
                }
            }
            
            return projectile.isOutOfBounds(getWidth(), getHeight());
        });
        
        // Check if we need to shoot at a new target
        if (targetMonster != null) {
            Monster currentTarget = findTargetMonster();
            if (currentTarget != targetMonster) {
                targetMonster = currentTarget;
                if (targetMonster != null) {
                    shootAtMonster(targetMonster);
                }
            }
        } else {
            targetMonster = findTargetMonster();
            if (targetMonster != null) {
                shootAtMonster(targetMonster);
            }
        }
    }
    
    private void handleMonsterHit(Monster monster) {
        boolean hasJamPower = monster.hasJamPower();
        boolean hasExtraLife = monster.hasExtraLife();
        boolean hasReverseInputPower = monster.hasReverseInputPower();
        boolean canSplit = monster.canSplit();
        
        // Decrease monster health
        monster.decreaseHealth();
        if (monster.getHealth() <= 0) {
            if (canSplit) {
                // Add child monsters
                Monster[] children = monster.split();
                for (Monster child : children) {
                    gameController.getMonsters().add(child);
                }
            }

            gameController.getMonsters().remove(monster);
            
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
        int shooterX = 10 + 30; // Center of shooter (10 + half of shooter size)
        int shooterY = (getHeight() - 60) / 2 + 30; // Center of shooter
        
        int targetX = monster.getX(getWidth()) + monster.getSize() / 2;
        int targetY = monster.getY(getHeight()) + monster.getSize() / 2;
        
        projectiles.add(new Projectile(shooterX, shooterY, targetX, targetY));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int height = getHeight();
        int width = getWidth();

        // Draw base (left side)
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 5, height);
        
        // Draw shooter at left center with rotation
        if (SHOOTER_IMAGE != null) {
            int shooterSize = 60;
            int shooterX = 10;
            int shooterY = (height - shooterSize) / 2;
            
            // Calculate rotation angle if there's a target
            double angle = 0;
            if (targetMonster != null) {
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
            
            // Restore the original transform
            g2d.setTransform(oldTransform);
        }

        // Draw projectiles
        for (Projectile projectile : projectiles) {
            projectile.draw(g);
        }

        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            monster.draw(g, width, height);
        }
    }
    
    private class Projectile {
        private double x, y;
        private double targetX, targetY;
        private static final double SPEED = 10.0;
        private static final int SIZE = 8;
        private static final double TARGET_THRESHOLD = 5.0; // Distance threshold to consider target reached
        
        public Projectile(int startX, int startY, int targetX, int targetY) {
            this.x = startX;
            this.y = startY;
            this.targetX = targetX;
            this.targetY = targetY;
        }
        
        public void update() {
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                x += (dx / distance) * SPEED;
                y += (dy / distance) * SPEED;
            }
        }
        
        public void draw(Graphics g) {
            g.setColor(Color.YELLOW);
            g.fillOval((int)x - SIZE/2, (int)y - SIZE/2, SIZE, SIZE);
        }
        
        public boolean isOutOfBounds(int width, int height) {
            return x < 0 || x > width || y < 0 || y > height;
        }
        
        public boolean hasReachedTarget() {
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance <= TARGET_THRESHOLD;
        }
        
        public boolean intersects(Monster monster, int panelWidth, int panelHeight) {
            int monsterX = monster.getX(panelWidth);
            int monsterY = monster.getY(panelHeight);
            int monsterSize = monster.getSize();
            
            // Simple circle-rectangle collision detection
            double closestX = Math.max(monsterX, Math.min(x, monsterX + monsterSize));
            double closestY = Math.max(monsterY, Math.min(y, monsterY + monsterSize));
            
            double distanceX = x - closestX;
            double distanceY = y - closestY;
            
            return (distanceX * distanceX + distanceY * distanceY) <= (SIZE/2 * SIZE/2);
        }
    }
}