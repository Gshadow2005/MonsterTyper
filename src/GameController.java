import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController {
    // Game components
    private CopyOnWriteArrayList<Monster> monsters;
    private Timer gameTimer;
    private Timer clearInputTimer;
    private JTextField inputField;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JPanel gamePanel;
    private Timer jamTimer; // Timer for keyboard jamming effect
    
    // Game state
    private int score;
    private int lives;
    private boolean gameRunning;
    private boolean isKeyboardJammed;
    private long jamEndTime;
    
    public GameController() {
        monsters = new CopyOnWriteArrayList<>(); 
        score = 0;
        lives = Constants.INITIAL_LIVES;
        gameRunning = true;
        isKeyboardJammed = false;
        
        // Initialize UI components
        initializeComponents();
        
        // Initialize clearInputTimer
        setupClearInputTimer();
        
        // Timer for checking jam status
        jamTimer = new Timer(100, e -> {
            if (isKeyboardJammed && System.currentTimeMillis() > jamEndTime) {
                endKeyboardJam();
            }
        });
        jamTimer.start();
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
                if (!isKeyboardJammed) {
                    checkInput();
                } else {
                    // Clear input while jammed
                    inputField.setText("");
                }
            }
        });
    }
    
    private void setupClearInputTimer() {
        clearInputTimer = new Timer(1500, e -> {
            inputField.setText("");
            ((Timer)e.getSource()).stop(); 
        });
        clearInputTimer.setRepeats(false);
    }
    
    public void startGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        resetGame();
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

        spawnMonster();
    }
    
    private void spawnMonster() {
        if (!gameRunning) return;
        
        // Create a new monster with random word
        String word = Constants.WORDS[Constants.RANDOM.nextInt(Constants.WORDS.length)];
        int x = Constants.WIDTH - Constants.MONSTER_SIZE;
        int y = Constants.RANDOM.nextInt(Constants.HEIGHT - 100 - Constants.MONSTER_SIZE);
        
        // Random chance to spawn a monster with keyboard jam power
        boolean hasJamPower = Constants.RANDOM.nextInt(100) < Constants.JAM_POWER_CHANCE;
        
        monsters.add(new Monster(x, y, word, hasJamPower));
    }
    
    private void updateGame() {
        if (!gameRunning || gamePanel == null) return;

        int panelWidth = gamePanel.getWidth();

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
        if (!gameRunning || isKeyboardJammed || inputField == null) return;
        
        String input = inputField.getText().trim().toLowerCase();
        if (input.isEmpty()) return;

        Monster monsterToRemove = null;
        for (Monster monster : monsters) {
            if (input.equals(monster.getWord().toLowerCase())) {
                monsterToRemove = monster;
                // Check if monster has keyboard jam power
                if (monster.hasJamPower()) {
                    startKeyboardJam();
                }
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

    public void increaseMonsterSpeed() {
        Constants.currentMonsterSpeed += 0.1;
        if (Constants.currentMonsterSpeed > Constants.MONSTER_MAX_SPEED) {
            Constants.currentMonsterSpeed = Constants.MONSTER_MAX_SPEED;
        }
    }
    
    // Add the missing methods for keyboard jam functionality
    private void startKeyboardJam() {
        isKeyboardJammed = true;
        jamEndTime = System.currentTimeMillis() + Constants.JAM_DURATION;
        
        if (inputField != null) {
            inputField.setEnabled(false);
            inputField.setBackground(new Color(255, 200, 200));
        }
    }
    
    private void endKeyboardJam() {
        isKeyboardJammed = false;
        
        if (inputField != null) {
            inputField.setEnabled(true);
            inputField.setBackground(Color.WHITE);
            inputField.setText("");
            inputField.requestFocus();
        }
    }
    
    private void increaseScore() {
        score += Constants.SCORE_PER_MONSTER;
        increaseMonsterSpeed();
        
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }
    
    private void decreaseLives() {
        lives--;
        
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
        }
        
        // Check for game over
        if (lives <= 0) {
            gameOver();
        }
    }
    
    private void gameOver() {
        gameRunning = false;
        
        if (gameTimer != null) {
            gameTimer.stop();
        }

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
        
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
        
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
        }
        
        gameRunning = true;
        
        if (gameTimer != null) {
            gameTimer.stop(); 
        }
        
        // Reset monster speed
        Constants.currentMonsterSpeed = Constants.MONSTER_INITIAL_SPEED;
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

    public void resumeGame() {
        if (!gameRunning && gameTimer != null) {
            gameTimer.start();
            gameRunning = true;
        }
    }
    
    // Getters for components
    public ArrayList<Monster> getMonsters() {
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
    
    public boolean isKeyboardJammed() {
        return isKeyboardJammed;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
}