import java.awt.*;
import java.awt.geom.AffineTransform;

public class Monster {
    private String word;
    //private Color color;
    private double relativeX, relativeY;
    private static final Image MONSTER_IMAGE = Toolkit.getDefaultToolkit().getImage(Monster.class.getResource("/assets/MonsterTyper_Zombie.gif"));
    
    public Monster(int x, int y, String word) {
        // this.x = x;
        // this.y = y;
        this.word = word;
        // this.color = new Color(
        //     Constants.RANDOM.nextInt(128) + 127, 
        //     Constants.RANDOM.nextInt(128) + 127, 
        //     Constants.RANDOM.nextInt(128) + 127
        // );

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;
    }
    
    public void update(int panelWidth) {
        double pixelsToMove = Constants.MONSTER_SPEED;
        double moveAmount = pixelsToMove / Constants.WIDTH;
        relativeX -= moveAmount;
    }
    
    public void draw(Graphics g, int panelWidth, int panelHeight) {
        Graphics2D g2d = (Graphics2D) g;

        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);
        int scaledSize = (int) (Constants.MONSTER_SIZE * Math.min(panelWidth / (double) Constants.WIDTH, panelHeight / (double) Constants.HEIGHT));

        // Original transform
        AffineTransform oldTransform = g2d.getTransform();

        // Flip the image horizontally
        g2d.translate(realX + scaledSize, realY);
        g2d.scale(-1, 1); 
        g2d.drawImage(MONSTER_IMAGE, 0, 0, scaledSize, scaledSize, null);

        // Restore transform for drawing text and health bar
        g2d.setTransform(oldTransform);

        // Draw Health Bar
        int healthBarHeight = 3;
        int healthBarWidth = scaledSize - 5;
        int healthBarX = realX;
        int healthBarY = realY - 10;

        g.setColor(Color.RED);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        
        // Draw word
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(word);
        int textX = realX + (scaledSize - textWidth) / 2;
        int textY = realY + scaledSize + fm.getAscent();

        g.drawString(word, textX, textY);
    }
    
    public int getX(int panelWidth) {
        return (int) (relativeX * panelWidth);
    }
    
    public String getWord() {
        return word;
    }
}