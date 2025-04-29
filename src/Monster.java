import java.awt.*;

public class Monster {
    private int x, y;
    private String word;
    private Color color;
    private double relativeX, relativeY;
    private boolean hasJamPower;
    private boolean hasExtraLife;
    
    public Monster(int x, int y, String word, boolean hasJamPower, boolean hasExtraLife) {
        this.x = x;
        this.y = y;
        this.word = word;
        this.hasJamPower = hasJamPower;
        this.hasExtraLife = hasExtraLife;
        
        if (hasJamPower) {
            this.color = Constants.JAM_MONSTER_COLOR;
        } else if (hasExtraLife) {
            this.color = Color.CYAN;
        } else {
            this.color = new Color(
                Constants.RANDOM.nextInt(128) + 127, 
                Constants.RANDOM.nextInt(128) + 127, 
                Constants.RANDOM.nextInt(128) + 127
            );
        }

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;
    }
    
    public void update(int panelWidth) {
        double moveAmount = Constants.MONSTER_SPEED / (double) panelWidth;
        if (hasJamPower) {
            moveAmount += (Constants.JAM_MONSTER_EXTRA_SPEED / (double) panelWidth);
        }
        relativeX -= moveAmount;   
    }
    
    public void draw(Graphics g, int panelWidth, int panelHeight) {
        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);
        int scaledSize = (int) (Constants.MONSTER_SIZE * Math.min(panelWidth / (double) Constants.WIDTH, 
                                panelHeight / (double) Constants.HEIGHT));

        // Draw monster body
        g.setColor(color);
        g.fillOval(realX, realY, scaledSize, scaledSize);
        
        // Special effects
        if (hasJamPower) {
            g.setColor(new Color(255, 255, 255, 100));
            g.fillOval(realX - 2, realY - 2, scaledSize + 4, scaledSize + 4);
        }
        
        if (hasExtraLife) {
            g.setColor(Color.YELLOW);
            g.fillOval(realX + scaledSize/4, realY - scaledSize/4, scaledSize/2, scaledSize/2);
        }
        
        // Draw word
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(word);
        g.drawString(word, realX + (scaledSize - textWidth)/2, realY + scaledSize/2);
    }
    
    public int getX() {
        return (int) (relativeX * Constants.WIDTH);
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