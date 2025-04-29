import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController {
    // Game components
    private CopyOnWriteArrayList<Monster> monsters; // Changed to thread-safe collection
    private Timer gameTimer;
    private JTextField inputField;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JPanel gamePanel; 
    
    // Game state
    private int score;
    private int lives;
    private boolean gameRunning;
    
    public GameController() {
        // Initialize game variables
        monsters = new CopyOnWriteArrayList<>(); // Changed to thread-safe collection
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
        if (gameTimer != null) {
            gameTimer.stop();
        }

        if (gameTimer != null) {
            resetGame();
        }

        gameRunning = true;

        gameTimer = new Timer(16, e -> {
            updateGame();
            if (gamePanel != null) {
                gamePanel.repaint();
            }

            if (Constants.RANDOM.nextInt(100) < Constants.SPAWN_CHANCE) {
                spawnMonster();
            }
        });
        
        gameTimer.start();

        if (inputField != null) {
            inputField.setText("");
        }

        if (clearInputTimer == null) {
            setupClearInputTimer();
        }
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

        int panelWidth = gamePanel.getWidth();
        
        // With CopyOnWriteArrayList, we can use the enhanced for loop safely
        ArrayList<Monster> monstersToRemove = new ArrayList<>();
        
        for (Monster monster : monsters) {
            monster.update(panelWidth);
            
            // Check if monster reached the base
            if (monster.getX(panelWidth) <= 0) {
                monstersToRemove.add(monster);
                decreaseLives();
            }
        }
        
        // Remove monsters after iteration
        if (!monstersToRemove.isEmpty()) {
            monsters.removeAll(monstersToRemove);
        }
    }
    
    private void checkInput() {
        if (!gameRunning) return;
        
        String input = inputField.getText().trim().toLowerCase();
        if (input.isEmpty()) return;

        Monster monsterToRemove = null;
        for (Monster monster : monsters) {
            if (input.equals(monster.getWord().toLowerCase())) {
                monsterToRemove = monster;
                break;
            }
        }

        if (monsterToRemove != null) {
            monsters.remove(monsterToRemove);
            increaseScore();
            inputField.setText("");
        } else {
            if (clearInputTimer.isRunning()) {
                clearInputTimer.restart(); 
            } else {
                clearInputTimer.start(); 
            }
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
        
        // Show game over 
        int option = JOptionPane.showConfirmDialog(gamePanel, 
            "Game Over!\nYour score: " + score + "\n\nPlay again?", 
            "Monster Typer", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            resetGame();
            startGame();
        } else {
            resetGame();
            fireGameOverEvent();
        }
    }

    public interface GameEventListener {
        void onGameOver();
    }
    
    private GameEventListener gameEventListener;
    
    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }
    
    private void fireGameOverEvent() {
        if (gameEventListener != null) {
            gameEventListener.onGameOver();
        }
    }    
    
    public void resetGame() {
        monsters.clear();
        score = 0;
        lives = Constants.INITIAL_LIVES;
        scoreLabel.setText("Score: " + score);
        livesLabel.setText("Lives: " + lives);
        gameRunning = true;
        if (gameTimer != null) {
            gameTimer.stop(); 
        }
    }

    private Timer clearInputTimer;

    private void setupClearInputTimer() {
        clearInputTimer = new Timer(1500, e -> {
            inputField.setText("");
            ((Timer)e.getSource()).stop(); // Stop the timer after clearing
        });
        clearInputTimer.setRepeats(false); // Only fire once
    }    

    public void stopGame() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameRunning = false;
        }
    }    

    public void pauseGame() {
        if (gameRunning && gameTimer != null) {
            gameTimer.stop();
            gameRunning = false;
        }
    }
    
    // Getters for components
    public ArrayList<Monster> getMonsters() {
        // Convert CopyOnWriteArrayList to ArrayList for compatibility
        return new ArrayList<>(monsters);
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
    
    public void setGamePanel(JPanel gamePanel) { 
        this.gamePanel = gamePanel;
    }
}