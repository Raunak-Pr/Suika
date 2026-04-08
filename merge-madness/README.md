# Merge Madness 🔮

An addictive Suika-style merge game built with **jMonkeyEngine 3.8.1-stable**.

Drop orbs into the arena. When two orbs of the same tier collide, they merge
into a bigger, higher-scoring orb. Don't let the pile overflow past the danger
line — or it's game over!

## Prerequisites

- **Java 11+** (JDK)
- **Maven 3.6+**

## Build & Run

### Quick start (compile + run):
```bash
mvn compile exec:exec
```

> **Note:** The POM is pre-configured with `-XstartOnFirstThread` for macOS.
> On Windows/Linux you can remove that flag from the `pom.xml` exec plugin,
> but it won't cause issues if left in on those platforms.

### Build a fat JAR (all dependencies included):
```bash
mvn clean package

# macOS:
java -XstartOnFirstThread -jar target/merge-madness-1.0-SNAPSHOT.jar

# Windows / Linux:
java -jar target/merge-madness-1.0-SNAPSHOT.jar
```

## How to Play

| Action           | Control              |
|------------------|----------------------|
| Aim              | Move mouse left/right |
| Drop orb         | Left click           |
| Start / Retry    | Left click           |

### Rules
- Same-tier orbs **merge** on contact → next tier (2→4→8→16→32→64→128→256→512→1K→2K)
- Chain merges for **combo multipliers** (x2, x3, x4...)
- If any orb stays above the **red danger line** for ~2 seconds → **Game Over**
- Your **best score** is saved between sessions

## Project Structure

```
merge-madness/
├── pom.xml                          # Maven config (jME 3.8.1-stable)
├── README.md
└── src/main/java/com/mergemadness/
    ├── MergeMadnessApp.java         # Entry point, jME SimpleApplication
    ├── GameManager.java             # Core game loop, state machine, input
    ├── PhysicsEngine.java           # 2D physics: gravity, collisions, merges
    ├── Orb.java                     # Orb data model (position, velocity, tier)
    ├── OrbTier.java                 # Tier definitions (radius, color, points)
    ├── OrbVisual.java               # jME3 visual representation of an orb
    ├── HudManager.java              # Score, combo, overlays (title/game-over)
    └── ParticleManager.java         # Merge explosion particle effects
```

## jMonkeyEngine SDK

This project is structured for compatibility with the **jMonkeyEngine SDK 3.8.1**.
To import:

1. Open jMonkeyEngine SDK
2. File → Open Project → select the `merge-madness` folder
3. The SDK will recognize the Maven `pom.xml`
4. Right-click project → Run

## Customization

- **Tier colors/sizes**: Edit `OrbTier.java` → `TIERS` array
- **Physics tuning**: Edit `PhysicsEngine.java` constants (GRAVITY, BOUNCE, etc.)
- **Drop weights**: Edit `OrbTier.java` → `DROP_WEIGHTS` array
- **Arena size**: Edit `PhysicsEngine.java` → `ARENA_HALF_WIDTH`, `ARENA_FLOOR_Y`
- **Danger timeout**: Edit `GameManager.java` → the `2.0f` second threshold

## License

Free to use for personal and educational purposes.
