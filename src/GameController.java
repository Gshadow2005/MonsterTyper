import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController {
    // Game components
    private CopyOnWriteArrayList<Monster> monsters;
    private Timer gameTimer;
    private Timer clearInputTimer;
    private JTextField inputField;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private GamePanel gamePanel; // Changed to GamePanel type for direct method access
    private Timer jamTimer;
    private PowerUpManager powerUpManager;
    
    // Game state
    private int score;
    private int lives;
    private boolean gameRunning;
    private boolean isKeyboardJammed;
    private boolean isInputScrambled; 
    private long jamEndTime;
    private long scrambleEndTime; 
    
    // Map for scrambled keys
    private Map<Character, Character> scrambledKeyMap;
    
    public GameController() {
        monsters = new CopyOnWriteArrayList<>(); 
        score = 0;
        lives = Constants.INITIAL_LIVES;
        gameRunning = true;
        isKeyboardJammed = false;
        isInputScrambled = false;
        
        // Initialize scrambled key map
        scrambledKeyMap = new HashMap<>();
        
        // Initialize PowerUpManager
        powerUpManager = new PowerUpManager(this);
        
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

            if (isInputScrambled && currentTime > scrambleEndTime) {
                endInputScramble();
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
        

        inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {

                if (isInputScrambled) {
                    char typedChar = e.getKeyChar();
                    if (Character.isLetter(typedChar)) {
                        char scrambledChar = scrambledKeyMap.getOrDefault(
                            Character.toLowerCase(typedChar), 
                            typedChar
                        );

                        if (Character.isUpperCase(typedChar)) {
                            scrambledChar = Character.toUpperCase(scrambledChar);
                        }

                        e.setKeyChar(scrambledChar);
                    }
                }
            }
            
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
        clearInputTimer = new Timer(2500, e -> { // 2.5 sec input delete
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

        // Get words for the current difficulty level
        String[] currentWords = Constants.DIFFICULTY_WORDS.get(Constants.currentDifficulty);
        String word = currentWords[Constants.RANDOM.nextInt(currentWords.length)];

        // Calculate spawn position
        int panelWidth = Constants.WIDTH;
        int panelHeight = Constants.HEIGHT;

        int x = panelWidth - Constants.MONSTER_SIZE; // Always spawn at the right edge

        // Define a vertical spawn range with a slight bias toward the top
        int centerY = panelHeight / 2;
        int spawnRangeY = panelHeight / 4; 
        int biasOffset = -panelHeight / 12; 

        // Randomize the y position within the vertical range
        int y = centerY - spawnRangeY / 2 + biasOffset + Constants.RANDOM.nextInt(spawnRangeY);

        // Ensure the monster fits within the panel bounds
        y = Math.max(0, Math.min(y, panelHeight - Constants.MONSTER_SIZE));

        // Create and add the monster
        Monster monster = new Monster(x, y, word);
        monsters.add(monster);
    }

    public void addMonster(Monster monster) {
        if (monster != null) {
            monsters.add(monster);
        }
    }
    
    private void updateGame() {
        if (!gameRunning || gamePanel == null) return;

        int panelWidth = gamePanel.getWidth();
        int thresholdX = (int) (panelWidth * 0.06); // 6% of the panel width

        ArrayList<Monster> monstersToRemove = new ArrayList<>();

        for (Monster monster : monsters) {
            // Only update monster position if not frozen
            if (!powerUpManager.areMonstersFrozen()) {
                monster.update(panelWidth);
            }
            
            // Check if monster reached the base
            if (monster.getX(panelWidth) <= thresholdX) {
                monstersToRemove.add(monster);
                decreaseLives();
                // Reset perfect streak when a monster reaches the base
                powerUpManager.resetStreak();
            }
        }

        // Remove monsters after iteration
        if (!monstersToRemove.isEmpty()) {
            monsters.removeAll(monstersToRemove);
        }
    }

    public void removeMonster(Monster monster) {
        if (monster != null && monsters.contains(monster)) {
            monsters.remove(monster);
        }
    }
    
    private void checkInput() {
        if (!gameRunning || isKeyboardJammed || inputField == null || gamePanel == null) return;
        
        String input = inputField.getText().trim().toLowerCase();
        if (input.isEmpty()) return;

        Monster monsterToHit = null;
        for (Monster monster : monsters) {
            if (input.equals(monster.getWord().toLowerCase())) {
                monsterToHit = monster;
                break;
            }
        }

        if (monsterToHit != null) {
            // Perfect hit registered - increment streak
            powerUpManager.registerPerfectHit();
            
            // Trigger animation through the GamePanel
            if (gamePanel instanceof GamePanel) {
                ((GamePanel) gamePanel).attackMonster(monsterToHit);
                inputField.setText(""); 
                return; // The animation will handle the rest
            }
            
            // If we don't have the proper GamePanel instance, fallback to direct handling
            boolean hasJamPower = monsterToHit.hasJamPower();
            boolean hasExtraLife = monsterToHit.hasExtraLife();
            boolean hasReverseInputPower = monsterToHit.hasReverseInputPower();
            boolean canSplit = monsterToHit.canSplit();
            
            // Trigger hit animation
            monsterToHit.hit();
            
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
                    startInputScramble();
                }
                
                increaseScore();
            }
            
            inputField.setText("");
            
        } else {
            // Incorrect input - reset the perfect streak
            powerUpManager.resetStreak();
            
            if (clearInputTimer.isRunning()) {
                clearInputTimer.restart(); 
            } else {
                clearInputTimer.start(); 
            }
        }
    }

    public void increaseMonsterSpeed() {
        Constants.currentMonsterSpeed += 0.01;
        if (Constants.currentMonsterSpeed > Constants.MONSTER_MAX_SPEED) {
            Constants.currentMonsterSpeed = Constants.MONSTER_MAX_SPEED;
        }
    }

    private void updateDifficultyLevel() {
        int oldDifficulty = Constants.currentDifficulty;

        if (score >= Constants.HARD_DIFFICULTY_THRESHOLD) {
            Constants.currentDifficulty = Constants.DIFFICULTY_HARD;
        } else if (score >= Constants.MEDIUM_DIFFICULTY_THRESHOLD) {
            Constants.currentDifficulty = Constants.DIFFICULTY_MEDIUM;
        } else {
            Constants.currentDifficulty = Constants.DIFFICULTY_EASY;
        }

        if (oldDifficulty != Constants.currentDifficulty) {
            notifyDifficultyChange();
        }
    }
    
    private void notifyDifficultyChange() {
        String difficultyName;
        Color notificationColor;
        
        switch (Constants.currentDifficulty) {
            case Constants.DIFFICULTY_MEDIUM:
                difficultyName = "Medium";
                notificationColor = Color.YELLOW;
                break;
            case Constants.DIFFICULTY_HARD:
                difficultyName = "Hard";
                notificationColor = Color.RED;
                break;
            default:
                difficultyName = "Easy";
                notificationColor = Color.GREEN;
                break;
        }
        
        // Show difficulty change notification
        JLabel notification = new JLabel("Difficulty increased to " + difficultyName + "!");
        notification.setForeground(notificationColor);
        notification.setFont(new Font("Arial", Font.BOLD, 21));
        notification.setHorizontalAlignment(SwingConstants.CENTER); 
        
        JLayeredPane layeredPane = gamePanel.getRootPane().getLayeredPane();
        int panelWidth = gamePanel.getWidth();
        int panelHeight = gamePanel.getHeight();
        
        // Set notification size
        int notificationWidth = 400;
        int notificationHeight = 40;
        
        // Center the notification on the game panel
        notification.setBounds(
            (panelWidth - notificationWidth) / 2, 
            panelHeight / 3, 
            notificationWidth, 
            notificationHeight
        );
        
        layeredPane.add(notification, JLayeredPane.POPUP_LAYER);
        
        // Remove notification after 2 seconds
        Timer notificationTimer = new Timer(2000, e -> {
            layeredPane.remove(notification);
            layeredPane.repaint();
            ((Timer)e.getSource()).stop();
        });
        notificationTimer.setRepeats(false);
        notificationTimer.start();
    }    
    
    // Input scrambling functionality
    public void startInputScramble() {
        isInputScrambled = true;
        scrambleEndTime = System.currentTimeMillis() + Constants.SCRAMBLE_DURATION;

        generateScrambledKeyMap();
        
        if (inputField != null) {
            inputField.setBackground(new Color(200, 200, 255)); 
            inputField.setText("");
            SwingUtilities.invokeLater(() -> {
                inputField.repaint();
                inputField.getParent().repaint();
            });
        }
    }
    
    private void generateScrambledKeyMap() {
        scrambledKeyMap.clear();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        //char[] scrambledAlphabet = alphabet.toCharArray();

        int shift = Constants.RANDOM.nextInt(10) + 5; 
        
        for (int i = 0; i < alphabet.length(); i++) {
            char originalChar = alphabet.charAt(i);
            char scrambledChar = alphabet.charAt((i + shift) % alphabet.length());
            scrambledKeyMap.put(originalChar, scrambledChar);
        }
    }
    
    private void endInputScramble() {
        isInputScrambled = false;
        scrambledKeyMap.clear();
        
        if (inputField != null) {
            inputField.setBackground(Color.WHITE);
            inputField.setText("");
            inputField.requestFocus();
            SwingUtilities.invokeLater(() -> {
                inputField.repaint();
                inputField.getParent().repaint();
            });
        }
    }
    
    // Keyboard jam functionality
    public void startKeyboardJam() {
        isKeyboardJammed = true;
        jamEndTime = System.currentTimeMillis() + Constants.JAM_DURATION;
        
        if (inputField != null) {
            inputField.setEnabled(false);
            inputField.setBackground(new Color(255, 200, 200)); // Light red background
            SwingUtilities.invokeLater(() -> {
                inputField.repaint();
                inputField.getParent().repaint();
            });
        }
    }
    
    private void endKeyboardJam() {
        isKeyboardJammed = false;
        
        if (inputField != null) {
            inputField.setEnabled(true);
            inputField.setBackground(Color.WHITE);
            inputField.setText("");
            inputField.requestFocus();
            SwingUtilities.invokeLater(() -> {
                inputField.repaint();
                inputField.getParent().repaint();
            });
        }
    }
    
    public void increaseScore() {
        score += Constants.SCORE_PER_MONSTER;
        increaseMonsterSpeed();
        
        // Check if we need to increase difficulty
        updateDifficultyLevel();
        
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }
    
    // Handling gaining an extra life 
    public void increaseLife() {
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
        
        // Play hurt sound if GamePanel is available
        if (gamePanel != null) {
            gamePanel.playHurtSound(); // This is the key change - call playHurtSound()
        }
        
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
        
        JDialog gameOverDialog = new JDialog();
        gameOverDialog.setTitle("Game Over");
        gameOverDialog.setModal(true);
        gameOverDialog.setSize(Constants.WIDTH, Constants.HEIGHT);
        gameOverDialog.setLocationRelativeTo(gamePanel);
        gameOverDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        GameOver gameOverPanel = new GameOver(
            e -> {
                gameOverDialog.dispose();
                resetGame();
                startGame();
            },

            e -> {
                gameOverDialog.dispose();
                resetGame();
                fireGameOverEvent();
            },
            score
        );
        
        gameOverDialog.add(gameOverPanel);
        gameOverDialog.setVisible(true);
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
        
        // Reset difficulty to easy
        Constants.currentDifficulty = Constants.DIFFICULTY_EASY;
        
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
        
        if (livesLabel != null) {
            livesLabel.setText("Lives: " + lives);
            livesLabel.setForeground(Color.WHITE); // Reset color
        }
        
        gameRunning = true;
        isKeyboardJammed = false;
        isInputScrambled = false;
        scrambledKeyMap.clear();
        
        if (inputField != null) {
            inputField.setEnabled(true);
            inputField.setBackground(Color.WHITE);
        }
        
        if (gameTimer != null) {
            gameTimer.stop(); 
        }
        
        // Reset monster speed
        Constants.currentMonsterSpeed = Constants.MONSTER_INITIAL_SPEED;
        
        // Reset power-up streak
        powerUpManager.resetStreak();

        if (gamePanel != null) {
            powerUpManager.setGamePanel(gamePanel);
        }
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
    
    // Getter for PowerUpManager
    public PowerUpManager getPowerUpManager() {
        return powerUpManager;
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
    
    // Modified to store as GamePanel type
    public void setGamePanel(JPanel panel) {
        if (panel instanceof GamePanel) {
            this.gamePanel = (GamePanel) panel;
            
            if (powerUpManager != null) {
                powerUpManager.setGamePanel(panel);
            }
        } else {
            System.out.println("Warning: Expected GamePanel instance but received different panel type");
            this.gamePanel = null;
        }
    }
    
    public boolean isKeyboardJammed() {
        return isKeyboardJammed;
    }
    
    public boolean isInputScrambled() {
        return isInputScrambled;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public JPanel getGamePanel() {
        return gamePanel;
    }
}