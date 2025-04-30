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
    
    // Difficulty levels
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    
    // Score thresholds for difficulty changes
    public static final int MEDIUM_DIFFICULTY_THRESHOLD = 200;
    public static final int HARD_DIFFICULTY_THRESHOLD = 400; 
    public static int currentDifficulty = DIFFICULTY_EASY; // Current difficulty level
    
    // Word lists for different difficulty levels
    public static final Map<Integer, String[]> DIFFICULTY_WORDS = loadWordsByDifficulty("assets/words.txt");
    
    // Random generator for the game
    public static final Random RANDOM = new Random();

    private static Map<Integer, String[]> loadWordsByDifficulty(String filePath) {
        Map<Integer, List<String>> wordLists = new HashMap<>();
        wordLists.put(DIFFICULTY_EASY, new ArrayList<>());
        wordLists.put(DIFFICULTY_MEDIUM, new ArrayList<>());
        wordLists.put(DIFFICULTY_HARD, new ArrayList<>());
        
        try (InputStream inputStream = Constants.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                System.err.println("File not found: " + filePath);
                return convertToArrayMap(wordLists); 
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                int currentSection = DIFFICULTY_EASY; // Default to easy
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // Check for section markers
                    if (line.equals("## EASY ##")) {
                        currentSection = DIFFICULTY_EASY;
                        continue;
                    } else if (line.equals("## MEDIUM ##")) {
                        currentSection = DIFFICULTY_MEDIUM;
                        continue;
                    } else if (line.equals("## HARD ##")) {
                        currentSection = DIFFICULTY_HARD;
                        continue;
                    }
                    
                    // Add word to appropriate list if not empty
                    if (!line.isEmpty()) {
                        wordLists.get(currentSection).add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading words from file: " + e.getMessage());
        }
        
        // Convert lists to arrays
        return convertToArrayMap(wordLists);
    }
    
    private static Map<Integer, String[]> convertToArrayMap(Map<Integer, List<String>> wordLists) {
        Map<Integer, String[]> result = new HashMap<>();
        
        for (Map.Entry<Integer, List<String>> entry : wordLists.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }

        if (result.get(DIFFICULTY_EASY).length == 0) {
            result.put(DIFFICULTY_EASY, new String[]{"error easy"});
        }
        if (result.get(DIFFICULTY_MEDIUM).length == 0) {
            result.put(DIFFICULTY_MEDIUM, new String[]{"error medium"});
        }
        if (result.get(DIFFICULTY_HARD).length == 0) {
            result.put(DIFFICULTY_HARD, new String[]{"error hard"});
        }
        
        return result;
    }
}