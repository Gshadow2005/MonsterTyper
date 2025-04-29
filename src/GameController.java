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
    private boolean isInputReversed;
    private long jamEndTime;
    private long reverseEndTime;
    
    public GameController() {
        monsters = new CopyOnWriteArrayList<>(); 
        score = 0;
        lives = Constants.INITIAL_LIVES;
        gameRunning = true;
        isKeyboardJammed = false;
        isInputReversed = false;
        
        // Initialize UI components
        initializeComponents();
        
        // Initialize clearInputTimer
        setupClearInputTimer();
        
        // Timer for checking jam status and other effects
        jamTimer = new Timer(100, e -> {
            long currentTime = System.currentTimeMillis();

            if (isKeyboardJammed && currentTime > jamEndTime) {
                endKeyboardJam();
            }

            if (isInputReversed && currentTime > reverseEndTime) {
                endInputReverse();
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
        
        Monster monster = new Monster(x, y, word);
        monsters.add(monster);
    }
    
    private void updateGame() {
        if (!gameRunning || gamePanel == null) return;

        int panelWidth = gamePanel.getWidth();
        //int panelHeight = gamePanel.getHeight();

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

        // reverse the input string
        if (isInputReversed) {
            input = new StringBuilder(input).reverse().toString();
        }

        Monster monsterToHit = null;
        for (Monster monster : monsters) {
            if (input.equals(monster.getWord().toLowerCase())) {
                monsterToHit = monster;
                break;
            }
        }

        if (monsterToHit != null) {
            boolean hasJamPower = monsterToHit.hasJamPower();
            boolean hasExtraLife = monsterToHit.hasExtraLife();
            boolean hasReverseInputPower = monsterToHit.hasReverseInputPower();
            boolean canSplit = monsterToHit.canSplit();
            
            // Decrease monster health
            monsterToHit.decreaseHealth();
            if (monsterToHit.getHealth() <= 0) {
                if (canSplit) {
                    // Add child monsters
                    Monster[] children = monsterToHit.split();
                    for (Monster child : children) {
                        monsters.add(child);
                    }
                }

                monsters.remove(monsterToHit);
                
                // Apply monster powers
                if (hasJamPower) {
                    startKeyboardJam();
                }
                if (hasExtraLife) {
                    increaseLife();
                }
                if (hasReverseInputPower) {
                    startInputReverse();
                }
            }

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
    
    // Input reversal functionality
    private void startInputReverse() {
        isInputReversed = true;
        reverseEndTime = System.currentTimeMillis() + Constants.REVERSE_DURATION;
        
        if (inputField != null) {
            inputField.setBackground(new Color(200, 200, 255)); 
            inputField.setText("");
        }
    }
    
    private void endInputReverse() {
        isInputReversed = false;
        
        if (inputField != null) {
            inputField.setBackground(Color.WHITE);
            inputField.setText("");
            inputField.requestFocus();
        }
    }
    
    // Keyboard jam functionality
    private void startKeyboardJam() {
        isKeyboardJammed = true;
        jamEndTime = System.currentTimeMillis() + Constants.JAM_DURATION;
        
        if (inputField != null) {
            inputField.setEnabled(false);
            inputField.setBackground(new Color(255, 200, 200)); // Light red background
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
    
    // Handling gaining an extra life 
    private void increaseLife() {
        lives++;
        
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
            livesLabel.setForeground(Color.GREEN);

            Timer colorTimer = new Timer(500, e -> {
                livesLabel.setForeground(Color.WHITE);
                ((Timer)e.getSource()).stop();
            });
            colorTimer.setRepeats(false);
            colorTimer.start();
        }
    }
    
    private void decreaseLives() {
        lives--;
        
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
            
            // Visual feedback that player lost a life
            livesLabel.setForeground(Color.RED);
            
            // Timer to reset color after a brief period
            Timer colorTimer = new Timer(500, e -> {
                livesLabel.setForeground(Color.WHITE);
                ((Timer)e.getSource()).stop();
            });
            colorTimer.setRepeats(false);
            colorTimer.start();
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
            livesLabel.setForeground(Color.WHITE); // Reset color
        }
        
        gameRunning = true;
        isKeyboardJammed = false;
        isInputReversed = false;
        
        if (inputField != null) {
            inputField.setEnabled(true);
            inputField.setBackground(Color.WHITE);
        }
        
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
    
    public boolean isInputReversed() {
        return isInputReversed;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
}