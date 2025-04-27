import java.awt.*;

public class Monster {
    private int x, y;
    private String word;
    private Color color;
    
    public Monster(int x, int y, String word) {
        this.x = x;
        this.y = y;
        this.word = word;
        this.color = new Color(
            Constants.RANDOM.nextInt(128) + 127, 
            Constants.RANDOM.nextInt(128) + 127, 
            Constants.RANDOM.nextInt(128) + 127
        );
    }
    
    public void update() {
        x -= Constants.MONSTER_SPEED;
    }
    
    public void draw(Graphics g) {
        // Draw monster body
        g.setColor(color);
        g.fillOval(x, y, Constants.MONSTER_SIZE, Constants.MONSTER_SIZE);
        
        // Draw word
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(word);
        g.drawString(word, x + (Constants.MONSTER_SIZE - textWidth) / 2, 
                    y + Constants.MONSTER_SIZE / 2);
    }
    
    public int getX() {
        return x;
    }
    
    public String getWord() {
        return word;
    }
}