import javax.swing.*;
import java.awt.*;

public class App extends JFrame {
    public App() {
        setTitle("Monster Typer");
        setSize(Constants.WIDTH, Constants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLocationRelativeTo(null);
        
        // Create the game controller
        GameController gameController = new GameController();
        
        // Create and add the game panel
        GamePanel gamePanel = new GamePanel(gameController);
        add(gamePanel, BorderLayout.CENTER);
        
        // Create the control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Create info panel (score and lives)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(gameController.getScoreLabel());
        infoPanel.add(gameController.getLivesLabel());
        infoPanel.setBackground(Color.DARK_GRAY);
        controlPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Add input field
        controlPanel.add(gameController.getInputField(), BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Start the game
        gameController.startGame();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new App().setVisible(true);
        });
    }
}

