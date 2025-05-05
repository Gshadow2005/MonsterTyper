import javax.swing.*;
import java.awt.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class PowerUpManager {
    private GameController gameController;
    private int perfectStreak = 0;
    private boolean monstersFrozen = false;
    private Timer freezeTimer;
    private Timer resetStreakTimer; 
    private JLabel streakLabel;
    private JLabel powerUpNotification;
    
    // Sound file paths with improved path resolution
    private final String SKIP_SOUND = "src/assets/Sounds/Skip1.wav";
    private final String KILL_ALL_SOUND = "src/assets/Sounds/Execution.wav";
    private final String FREEZE_SOUND = "src/assets/Sounds/Freezing.wav";

    public PowerUpManager(GameController gameController) {
        this.gameController = gameController;
        initializeUIComponents();
    }

    private void initializeUIComponents() {
        // Label to show current streak
        streakLabel = new JLabel("Perfect Streak: 0");
        streakLabel.setForeground(Color.YELLOW);
        streakLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Notification label for power-ups
        powerUpNotification = new JLabel();
        powerUpNotification.setForeground(Color.CYAN);
        powerUpNotification.setFont(new Font("Arial", Font.BOLD, 16));
        powerUpNotification.setHorizontalAlignment(SwingConstants.CENTER);
        powerUpNotification.setVisible(false);
        
        // Add components to game panel if available
        if (gameController.getGamePanel() != null) {
            JPanel gamePanel = gameController.getGamePanel();
            gamePanel.setLayout(new BorderLayout());
            
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            topPanel.setOpaque(false);
            topPanel.add(streakLabel);
            
            gamePanel.add(topPanel, BorderLayout.NORTH);
            gamePanel.add(powerUpNotification, BorderLayout.SOUTH);
        }
    }

    /**
     * Plays a sound file
     * @param soundFilePath Path to the sound file
     */
    private void playSound(String soundFilePath) {
        try {
            File soundFile = new File(soundFilePath);
            
            // Check if file exists to avoid "path not found" errors
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundFilePath);
                // Try alternative path resolution using ClassLoader
                URL resourceUrl = getClass().getClassLoader().getResource(soundFilePath.replace("src/", ""));
                if (resourceUrl != null) {
                    soundFile = new File(resourceUrl.getFile());
                } else {
                    // Try parent directory as a fallback
                    soundFile = new File("../" + soundFilePath);
                    if (!soundFile.exists()) {
                        System.err.println("Could not find sound file using alternative paths");
                        return;
                    }
                }
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound effect: " + e.getMessage());
        }
    }

    public void registerPerfectHit() {
        if (resetStreakTimer != null && resetStreakTimer.isRunning()) {
            resetStreakTimer.stop();
        }
        
        perfectStreak++;
        updateStreakDisplay();

        // Every 15 perfect hits triggers a random power-up
        if (perfectStreak >= 10) {
            activateRandomPowerUp();
            perfectStreak = 0;
            updateStreakDisplay();
        }
    }

    private void updateStreakDisplay() {
        if (streakLabel != null) {
            streakLabel.setText("Perfect Streak: " + perfectStreak + "/10");
            
            // Visual feedback when getting close to power-up
            if (perfectStreak >= 8) {
                streakLabel.setForeground(new Color(255, 165, 0)); // Orange
            } else {
                streakLabel.setForeground(Color.YELLOW);
            }
        }
    }

    public void resetStreak() {
        if (resetStreakTimer != null && resetStreakTimer.isRunning()) {
            return;
        }

        if (streakLabel != null) {
            streakLabel.setForeground(Color.RED);
        }
        
        resetStreakTimer = new Timer(1000, e -> {
            perfectStreak = 0;
            updateStreakDisplay();
            resetStreakTimer.stop();
        });
        resetStreakTimer.setRepeats(false);
        resetStreakTimer.start();
    }

    public boolean areMonstersFrozen() {
        return monstersFrozen;
    }

    private void activateRandomPowerUp() {
        int choice = new Random().nextInt(3); // 0, 1, or 2

        if (choice == 0) {
            activateFreezePowerUp();
        } else if (choice == 1) {
            activateSkipWordPowerUp();
        } else {
            activateKillAllMonstersPowerUp();
        }
    }

    private void activateFreezePowerUp() {
        monstersFrozen = true;
        showPowerUpNotification("Freeze Activated!", Color.CYAN);
        
        // Play freeze sound effect
        playSound(FREEZE_SOUND);

        if (freezeTimer != null && freezeTimer.isRunning()) {
            freezeTimer.stop();
        }

        freezeTimer = new Timer(3000, e -> {
            monstersFrozen = false;
            freezeTimer.stop();
            showPowerUpNotification("", Color.WHITE); 
        });

        freezeTimer.start();
        
        // Visual feedback for freeze effect
        if (gameController.getGamePanel() != null) {
            gameController.getGamePanel().setBackground(new Color(200, 230, 255)); // Light blue tint
            Timer restoreTimer = new Timer(3000, ev -> {
                gameController.getGamePanel().setBackground(Color.BLACK);
            });
            restoreTimer.setRepeats(false);
            restoreTimer.start();
        }
    }

    private void activateSkipWordPowerUp() {
        if (gameController.getMonsters().size() > 1) {
            int monsterIndex = 1; 

            if (gameController.getMonsters().size() > 2) {
                monsterIndex = 1 + new Random().nextInt(gameController.getMonsters().size() - 1);
            }
            
            Monster toRemove = gameController.getMonsters().get(monsterIndex);
            gameController.removeMonster(toRemove);
            gameController.increaseScore();
            showPowerUpNotification("Word Skipped!", Color.GREEN);
            
            // Play skip sound effect
            playSound(SKIP_SOUND);
            
            // Flash effect for skip power-up
            if (gameController.getGamePanel() != null) {
                JPanel panel = gameController.getGamePanel();
                Color original = panel.getBackground();
                panel.setBackground(new Color(200, 255, 200)); // Light green
                Timer restoreTimer = new Timer(500, e -> {
                    panel.setBackground(original);
                });
                restoreTimer.setRepeats(false);
                restoreTimer.start();
            }
        } else if (gameController.getMonsters().size() == 1) {
            showPowerUpNotification("No Other Words to Skip!", Color.YELLOW);
            playSound(SKIP_SOUND);
        }
    }
    
    private void activateKillAllMonstersPowerUp() {
        if (!gameController.getMonsters().isEmpty()) {
            int monsterCount = gameController.getMonsters().size();
            
            // Play kill all monsters sound effect
            playSound(KILL_ALL_SOUND);
            
            for (int i = monsterCount - 1; i >= 0; i--) {
                Monster monster = gameController.getMonsters().get(i);
                gameController.removeMonster(monster);
                gameController.increaseScore();
            }
            
            showPowerUpNotification("All Monsters Defeated!", Color.RED);
            
            // Flash effect for kill all monsters power-up
            if (gameController.getGamePanel() != null) {
                JPanel panel = gameController.getGamePanel();
                Color original = panel.getBackground();
                panel.setBackground(new Color(255, 150, 150)); // Light red
                Timer restoreTimer = new Timer(500, e -> {
                    panel.setBackground(original);
                });
                restoreTimer.setRepeats(false);
                restoreTimer.start();
            }
        }
    }

    private void showPowerUpNotification(String message, Color color) {
        if (powerUpNotification != null) {
            powerUpNotification.setText(message);
            powerUpNotification.setForeground(color);
            powerUpNotification.setVisible(!message.isEmpty());
            
            if (!message.isEmpty()) {
                // Auto-hide notification after 2 seconds
                Timer hideTimer = new Timer(2000, e -> {
                    powerUpNotification.setVisible(false);
                });
                hideTimer.setRepeats(false);
                hideTimer.start();
            }
        }
    }

    // Call this when the game panel is available
    public void setGamePanel(JPanel gamePanel) {
        if (gamePanel != null) {
            gamePanel.setLayout(new BorderLayout());
            
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            topPanel.setOpaque(false);
            topPanel.add(streakLabel);
            
            gamePanel.add(topPanel, BorderLayout.NORTH);
            gamePanel.add(powerUpNotification, BorderLayout.SOUTH);
        }
    }
}