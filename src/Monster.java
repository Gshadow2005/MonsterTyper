import java.awt.*;

public class Monster {
    private String word;
    private Color color;
    private double relativeX, relativeY;
    
    public Monster(int x, int y, String word) {
        // this.x = x;
        // this.y = y;
        this.word = word;
        this.color = new Color(
            Constants.RANDOM.nextInt(128) + 127, 
            Constants.RANDOM.nextInt(128) + 127, 
            Constants.RANDOM.nextInt(128) + 127
        );

        relativeX = x / (double) Constants.WIDTH;
        relativeY = y / (double) Constants.HEIGHT;
    }
    
    public void update(int panelWidth) {
        double pixelsToMove = Constants.MONSTER_SPEED;
        double moveAmount = pixelsToMove / Constants.WIDTH;
        relativeX -= moveAmount;
    }
    
    public void draw(Graphics g, int panelWidth, int panelHeight) {
        // Calculate the new position based on the relative position
        int realX = (int) (relativeX * panelWidth);
        int realY = (int) (relativeY * panelHeight);

        // Scale monster size based on panel size
        int scaledSize = (int) (Constants.MONSTER_SIZE * Math.min(panelWidth / (double) Constants.WIDTH, panelHeight / (double) Constants.HEIGHT));

        // Draw monster body
        g.setColor(color);
        g.fillOval(realX, realY, scaledSize, scaledSize);
        
        // Draw word
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(word);
        g.drawString(word, realX + (scaledSize - textWidth) / 2, 
                    realY + scaledSize / 2);
    }
    
    public int getX(int panelWidth) {
        return (int) (relativeX * panelWidth);
    }
    
    public String getWord() {
        return word;
    }
}