import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Constants {
    // Game dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 700;
    
    // Monster properties
    public static final int MONSTER_SIZE = 50;
    public static final double MONSTER_INITIAL_SPEED = 0.5;
    public static final double MONSTER_MAX_SPEED = 10;
    
    // Monster power chances (percentage)
    public static final int SPLIT_CHANCE = 2;
    public static final int JAM_POWER_CHANCE = 100;  
    public static final int REVERSE_POWER_CHANCE = 5; 
    public static final int EXTRA_LIFE_CHANCE = 4;  
    
    // Power duration
    public static final long JAM_DURATION = 3000;    
    public static final long SCRAMBLE_DURATION = 5000; 

    public static double currentMonsterSpeed = MONSTER_INITIAL_SPEED;
    
    // Game settings
    public static final int INITIAL_LIVES = 5;
    public static final int SCORE_PER_MONSTER = 10;
    public static final int SPAWN_CHANCE = 1; 
    
    // Difficulty levels
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    
    // Score thresholds for difficulty changes
    public static final int MEDIUM_DIFFICULTY_THRESHOLD = 200;
    public static final int HARD_DIFFICULTY_THRESHOLD = 400;
    
    // Current difficulty level
    public static int currentDifficulty = DIFFICULTY_EASY;
    
    // Word files
    private static final String WORDS_FOLDER = "assets/words/";
    private static final String EASY_WORDS_FILE = WORDS_FOLDER + "easy_words.txt";
    private static final String MEDIUM_WORDS_FILE = WORDS_FOLDER + "medium_words.txt";
    private static final String HARD_WORDS_FILE = WORDS_FOLDER + "hard_words.txt";

    // Add these with your other constants:
    public static final int FREEZE_DURATION = 3000; // 3 seconds
    public static final int PERFECT_HITS_FOR_POWERUP = 5; // Number of perfect hits needed

    // Fortress properties
    public static final int FORT_WIDTH = 500; 
    public static final int FORT_HEIGHT = 500; 

    public static final Map<Integer, String[]> DIFFICULTY_WORDS = loadAllWordFiles();
    
    // Random generator for the game
    public static final Random RANDOM = new Random();

    private static Map<Integer, String[]> loadAllWordFiles() {
        Map<Integer, String[]> difficultyWords = new HashMap<>();
        
        // Load words
        difficultyWords.put(DIFFICULTY_EASY, loadWordsFromFile(EASY_WORDS_FILE));
        difficultyWords.put(DIFFICULTY_MEDIUM, loadWordsFromFile(MEDIUM_WORDS_FILE));
        difficultyWords.put(DIFFICULTY_HARD, loadWordsFromFile(HARD_WORDS_FILE));
        
        // ERRROR HANDLING 
        if (difficultyWords.get(DIFFICULTY_EASY).length == 0) {
            difficultyWords.put(DIFFICULTY_EASY, new String[]{"ERROR EASY"});
        }
        if (difficultyWords.get(DIFFICULTY_MEDIUM).length == 0) {
            difficultyWords.put(DIFFICULTY_MEDIUM, new String[]{"ERROR MEDIUM"});
        }
        if (difficultyWords.get(DIFFICULTY_HARD).length == 0) {
            difficultyWords.put(DIFFICULTY_HARD, new String[]{"ERROR HARD"});
        }
        
        return difficultyWords;
    }
    
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