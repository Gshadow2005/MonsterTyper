## Monster Typer

Monster Typer is a typing defense game developed with Java and Swing. In this game, monsters approach the base carrying letters or words. The player must type them correctly to defeat the monsters before they reach the base.

### Features

- Game difficulty increases with faster monsters and longer words
- Some monsters split into smaller ones when defeated
- Some monsters temporarily jam the player's typing
- Boss monsters require full phrases to defeat
- Power-ups are unlocked by achieving perfect typing streaks

### Technical Information

- Built using Java with Swing for the UI
- Uses simple 2D graphics
- Words are loaded from a text file
- Monster movement is linear
- Difficulty increases over time

### Installation

1. Ensure you have a bin directory:
```bash
mkdir -p bin
```

2. Compile the source files:
```bash
javac -d bin src/*.java
```

3. Run the application:
```bash
java -cp bin App
```

### Current Status

The game is currently under development. Basic functionality is implemented.
