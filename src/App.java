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
        controlPanel.setBackground(Color.DARK_GRAY);
        
        // Create info panel (score and lives)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(Color.DARK_GRAY);
        
        // Make labels more prominent
        gameController.getScoreLabel().setFont(new Font("Arial", Font.BOLD, 14));
        gameController.getLivesLabel().setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(gameController.getScoreLabel());
        infoPanel.add(Box.createHorizontalStrut(20)); // Add spacing
        infoPanel.add(gameController.getLivesLabel());
        
        controlPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Configure input field
        JTextField inputField = gameController.getInputField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField.setMargin(new Insets(5, 5, 5, 5));
        
        // Add status label for jam notifications
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.ORANGE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to control panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(statusLabel, BorderLayout.SOUTH);
        controlPanel.add(inputPanel, BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Start the game
        gameController.startGame();
        
        // Add listener for jam events (if you want visual feedback)
        // This would require adding a method in GameController to register listeners
        // For example:
        // gameController.addGameEventListener(new GameEventListener() {
        //     public void onKeyboardJam(boolean jammed) {
        //         if (jammed) {
        //             statusLabel.setText("KEYBOARD JAMMED!");
        //             inputField.setBackground(new Color(255, 200, 200));
        //         } else {
        //             statusLabel.setText(" ");
        //             inputField.setBackground(Color.WHITE);
        //         }
        //     }
        // });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}