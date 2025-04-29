import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Constants {
    // Game dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 700;
    
    // Monster properties
    public static final int MONSTER_SIZE = 50;
    public static final double MONSTER_INITIAL_SPEED = 0.5;
    public static final double MONSTER_MAX_SPEED = 3.0;
    
    // Monster power chances (percentage)
    public static final int SPLIT_CHANCE = 1;    
    public static final int JAM_POWER_CHANCE = 3;  
    public static final int REVERSE_POWER_CHANCE = 3; 
    public static final int EXTRA_LIFE_CHANCE = 1;  
    
    // Power duration
    public static final long JAM_DURATION = 3000;    
    public static final long SCRAMBLE_DURATION = 5000; 

    public static double currentMonsterSpeed = MONSTER_INITIAL_SPEED;
    
    // Game settings
    public static final int INITIAL_LIVES = 5;
    public static final int SCORE_PER_MONSTER = 10;
    public static final int SPAWN_CHANCE = 1; 
    
    // Word list for monsters
    public static final String[] WORDS = loadWordsFromFile("assets/words.txt");
    
    // Random generator for the game
    public static final Random RANDOM = new Random();

    private static String[] loadWordsFromFile(String filePath) {
        List<String> wordList = new ArrayList<>();
        try (InputStream inputStream = Constants.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                System.err.println("File not found: " + filePath);
                return new String[0]; 
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        wordList.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading words from file: " + e.getMessage());
        }
        return wordList.toArray(new String[0]);
    }
}