# BYOW Pac-Man Style Dungeon Game

A Java dungeon game built from the BYOW project base and expanded into a small arcade-style survival game.

You explore randomly generated floors, collect pellet dots for score, avoid a chasing enemy, and race against a 2-minute timer. Reaching the exit sends you to the next floor, and getting caught or running out of time ends the run.

## Features

- Randomly generated dungeon floors
- Avatar name entry and seeded world generation
- WASD movement
- Arrow key movement
- Pellet-style floor dots that are collected for score
- Enemy that chases the player
- 2-minute timer
- Floor-to-floor progression
- Game-over screen with:
  - Player Name
  - Final Score
  - Floors Cleared
  - Play Again / Quit options

## Controls

- `N`: New game
- `Q`: Quit from menu
- `W A S D`: Move
- Arrow keys: Move
- `Backspace`: Delete a character while entering seed or avatar name
- Avatar start:
  - press `"1"`
  - or press `"Enter"`
- Game over screen:
  - `P`: Play again
  - `Q`: Quit

## How To Play

1. Start a new game.
2. Enter a numeric seed, then press `S`.
3. Enter an avatar name, then press `1` or `Enter`.
4. Move around the map collecting `.` pellets.
5. Avoid the enemy.
6. Reach the exit door to advance to the next floor.
7. Survive until time runs out or until the enemy catches you.

## Run The Game

This project includes batch scripts for build and run on Windows.

### Quick Start

Use:

```powershell
Ctrl+Shift+B
```

In this project, `Ctrl+Shift+B` is configured to build and run the game in VS Code.

### Manual Run

From the project folder:

```powershell
.\run-game.bat
```

### Build Only

```powershell
.\build-game.bat
```

## Requirements

- Windows
- Java 21 or newer
- VS Code is recommended for the included task setup

The batch files use `JAVA_HOME` if it is set. Otherwise, they look for `java.exe` and `javac.exe` on your `PATH`.

If the scripts cannot find Java, install a JDK and either set `JAVA_HOME` to the JDK folder or add the JDK `bin` folder to `PATH`.

## Project Structure

- `byow/Core`: game logic
- `byow/TileEngine`: tile rendering system
- `edu/princeton/cs/introcs`: local `StdDraw` compatibility layer

## Notes

- The enemy uses chase logic to follow the player through the dungeon.
- Score increases when you collect fresh pellets.
