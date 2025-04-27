import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private GameController gameController;
    
    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(Color.BLACK);
        gameController.setGamePanel(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw base (left side)
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 5, Constants.HEIGHT);
        
        // Draw monsters
        ArrayList<Monster> monsters = gameController.getMonsters();
        for (Monster monster : monsters) {
            monster.draw(g);
        }
    }
}