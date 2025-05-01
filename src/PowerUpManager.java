import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PowerUpManager {
    private GameController gameController;
    private int perfectStreak = 0;
    private boolean monstersFrozen = false;
    private Timer freezeTimer;
    private JLabel streakLabel;
    private JLabel powerUpNotification;

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

    public void registerPerfectHit() {
        perfectStreak++;
        updateStreakDisplay();

        // Every 5 perfect hits triggers a random power-up
        if (perfectStreak >= 15) {
            activateRandomPowerUp();
            perfectStreak = 0;
            updateStreakDisplay();
        }
    }

    private void updateStreakDisplay() {
        if (streakLabel != null) {
            streakLabel.setText("Perfect Streak: " + perfectStreak + "/15");
            
            // Visual feedback when getting close to power-up
            if (perfectStreak >= 3) {
                streakLabel.setForeground(new Color(255, 165, 0)); // Orange
            } else {
                streakLabel.setForeground(Color.YELLOW);
            }
        }
    }

    public void resetStreak() {
        perfectStreak = 0;
        updateStreakDisplay();
    }

    public boolean areMonstersFrozen() {
        return monstersFrozen;
    }

    private void activateRandomPowerUp() {
        int choice = new Random().nextInt(2); // 0 or 1

        if (choice == 0) {
            activateFreezePowerUp();
        } else {
            activateSkipWordPowerUp();
        }
    }

    private void activateFreezePowerUp() {
        monstersFrozen = true;
        showPowerUpNotification("FREEZE POWER-UP ACTIVATED!", Color.CYAN);

        if (freezeTimer != null && freezeTimer.isRunning()) {
            freezeTimer.stop();
        }

        freezeTimer = new Timer(3000, e -> {
            monstersFrozen = false;
            freezeTimer.stop();
            showPowerUpNotification("", Color.WHITE); // Clear notification
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
        if (!gameController.getMonsters().isEmpty()) {
            Monster toRemove = gameController.getMonsters().get(0);
            gameController.removeMonster(toRemove);
            gameController.increaseScore();
            showPowerUpNotification("WORD SKIPPED!", Color.GREEN);
            
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