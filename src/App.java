import javax.swing.*;
import java.awt.*;

public class App extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MainMenu mainMenu;
    private JPanel gameContainer;
    private GameController gameController;
    
    public App() {
        setTitle("Monster Typer");
        setSize(Constants.WIDTH, Constants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 700));
        setResizable(true);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        //Main menu
        mainMenu = new MainMenu(e -> startGame());
        gameContainer = new JPanel(new BorderLayout());
        
        mainPanel.add(mainMenu, "MENU");
        mainPanel.add(gameContainer, "GAME");
        
        // Start with menu
        cardLayout.show(mainPanel, "MENU");
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    private void startGame() {
        if (gameController == null) {
            gameController = new GameController();
            

            GamePanel gamePanel = new GamePanel(gameController);
            gameContainer.add(gamePanel, BorderLayout.CENTER);
            
            // Create control panel
            JPanel controlPanel = new JPanel(new BorderLayout());
            
            // Create info panel (score and lives)
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            infoPanel.add(gameController.getScoreLabel());
            infoPanel.add(gameController.getLivesLabel());
            infoPanel.setBackground(Color.DARK_GRAY);
            
            // Add back to menu button
            JButton backButton = new JButton("Back to Menu");
            backButton.addActionListener(e -> returnToMenu());
            infoPanel.add(Box.createHorizontalGlue());
            infoPanel.add(backButton);
            
            controlPanel.add(infoPanel, BorderLayout.NORTH);
            
            // Add input field
            controlPanel.add(gameController.getInputField(), BorderLayout.CENTER);
            
            gameContainer.add(controlPanel, BorderLayout.SOUTH);
        }
        
        // Switch to game screen
        cardLayout.show(mainPanel, "GAME");
        
        // Request focus on input field and start game
        gameController.getInputField().requestFocus();
        gameController.startGame();
    }
    
    private void returnToMenu() {
        if (gameController != null) {
            gameController.pauseGame();
        }
        
        // Switch to menu screen
        cardLayout.show(mainPanel, "MENU");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new App().setVisible(true);
        });
    }
}