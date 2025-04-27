import java.util.Random;

public class Constants {
    // Game dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    // Monster properties
    public static final int MONSTER_SIZE = 40;
    public static final int MONSTER_SPEED = 1;
    
    // Game settings
    public static final int INITIAL_LIVES = 3;
    public static final int SCORE_PER_MONSTER = 10;
    public static final int SPAWN_CHANCE = 2; // percent chance per frame
    
    // Word list for monsters
    public static final String[] WORDS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", 
                                        "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    
    // Random generator for the game
    public static final Random RANDOM = new Random();
}