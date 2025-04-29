import java.util.Random;

public class Constants {
    // Game dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 700;
    
    // Monster properties
    public static final int MONSTER_SIZE = 50;
    public static final int MONSTER_SPEED = 1;
    
    // Game settings
    public static final int INITIAL_LIVES = 1000;
    public static final int SCORE_PER_MONSTER = 10;
    public static final int SPAWN_CHANCE = 2; // percent chance per frame
    
    // Word list for monsters
    public static final String[] WORDS = {"Tree", "Bird", "Snow", "Infinite", "Nebula", "Enigma", "Sand", 
                                            "Leaf", "Wanderlust", "Labyrinth", "Grass", "core", "music", "ocean", "moon", "star", "sun", "sky", "cloud", 
                                            "rain", "wind", "fire", "earth", "water", "lightning", "shadow", "dream", "whisper", "echo", "pulse", "wave", "spark"};
                                        
    
    // Random generator for the game
    public static final Random RANDOM = new Random();
}