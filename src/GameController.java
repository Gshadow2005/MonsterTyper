import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GameController {
    // Game components
    private ArrayList<Monster> monsters;
    private Timer gameTimer;
    private JTextField inputField;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JPanel gamePanel; // Changed from GamePanel to JPanel
    
    // Game state
    private int score;
    private int lives;
    private boolean gameRunning;
    
    public GameController() {
        // Initialize game variables
        monsters = new ArrayList<>();
        score = 0;
        lives = Constants.INITIAL_LIVES;
        gameRunning = true;
        
        // Initialize UI components
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Create score and lives labels
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setForeground(Color.WHITE);
        
        livesLabel = new JLabel("Lives: " + lives);
        livesLabel.setForeground(Color.WHITE);
        
        // Create input field with key listener
        inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkInput();
            }
        });
    }
    
    public void startGame() {
        // Create and start game timer
        gameTimer = new Timer(16, e -> {
            updateGame();
            if (gamePanel != null) {
                gamePanel.repaint();
            }
            
            // Spawn new monster occasionally
            if (Constants.RANDOM.nextInt(100) < Constants.SPAWN_CHANCE) {
                spawnMonster();
            }
        });
        
        gameTimer.start();
        
        // Spawn first monster
        spawnMonster();
    }
    
    private void spawnMonster() {
        if (!gameRunning) return;
        
        // Create a new monster with random word
        String word = Constants.WORDS[Constants.RANDOM.nextInt(Constants.WORDS.length)];
        int x = Constants.WIDTH - Constants.MONSTER_SIZE;
        int y = Constants.RANDOM.nextInt(Constants.HEIGHT - 100 - Constants.MONSTER_SIZE);
        
        monsters.add(new Monster(x, y, word));
    }
    
    private void updateGame() {
        if (!gameRunning) return;
        
        // Update all monsters
        ArrayList<Monster> monstersToRemove = new ArrayList<>();
        for (Monster monster : monsters) {
            monster.update();
            
            // Check if monster reached the base
            if (monster.getX() <= 0) {
                monstersToRemove.add(monster);
                decreaseLives();
            }
        }
        
        // Remove monsters that reached the base
        monsters.removeAll(monstersToRemove);
    }
    
    private void checkInput() {
        if (!gameRunning) return;
        
        String input = inputField.getText().trim().toLowerCase();
        if (input.isEmpty()) return;
        
        // Check if input matches any monster's word
        Monster monsterToRemove = null;
        for (Monster monster : monsters) {
            if (input.equals(monster.getWord().toLowerCase())) {
                monsterToRemove = monster;
                break;
            }
        }
        
        // Remove matched monster and update score
        if (monsterToRemove != null) {
            monsters.remove(monsterToRemove);
            increaseScore();
            inputField.setText("");
        }
    }
    
    private void increaseScore() {
        score += Constants.SCORE_PER_MONSTER;
        scoreLabel.setText("Score: " + score);
    }
    
    private void decreaseLives() {
        lives--;
        livesLabel.setText("Lives: " + lives);
        
        // Check for game over
        if (lives <= 0) {
            gameOver();
        }
    }
    
    private void gameOver() {
        gameRunning = false;
        gameTimer.stop();
        
        JOptionPane.showMessageDialog(gamePanel, 
            "Game Over!\nYour score: " + score, 
            "Monster Typer", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // Reset game
        resetGame();
    }
    
    private void resetGame() {
        monsters.clear();
        score = 0;
        lives = Constants.INITIAL_LIVES;
        scoreLabel.setText("Score: " + score);
        livesLabel.setText("Lives: " + lives);
        gameRunning = true;
        gameTimer.start();
    }
    
    // Getters for components
    public ArrayList<Monster> getMonsters() {
        return monsters;
    }
    
    public JTextField getInputField() {
        return inputField;
    }
    
    public JLabel getScoreLabel() {
        return scoreLabel;
    }
    
    public JLabel getLivesLabel() {
        return livesLabel;
    }
    
    public void setGamePanel(JPanel gamePanel) { // Changed from GamePanel to JPanel
        this.gamePanel = gamePanel;
    }
}