# 🐱🐱 Catppuccino 🐱🐱

![curled_1.png](src/main/resources/white_cat/curled/curled_1.png)![curled_1.png](src/main/resources/orange_cat/curled/curled_1.png)![curled_1.png](src/main/resources/grey_tabby_cat/curled/curled_1.png)![curled_1.png](src/main/resources/calico_cat/curled/curled_1.png)

A lightweight and adorable desktop virtual pet cat. It will roam freely on your desktop, play, sleep, and keep you
company during your work or study time, bringing a touch of relaxation and delight.

## Preview

![video.gif](doc/video.gif)

![curled_1.png](src/main/resources/white_cat/curled/curled_1.png)![curled_1.png](src/main/resources/orange_cat/curled/curled_1.png)![curled_1.png](src/main/resources/grey_tabby_cat/curled/curled_1.png)![curled_1.png](src/main/resources/calico_cat/curled/curled_1.png)

## Feature

- **Adorable Interactions**: The kitten has multiple states, including walking, stretching, napping.
- **Desktop Roaming**: It intelligently moves around the edges of your screen without interrupting your work.
- **Low Resource Usage**: Built with Java **Swing**, it uses minimal system resources.
- **Cross-Platform**: Supports Windows, macOS and Linux.
- **Four Skins Are Randomly Selected**: calico cat, grey tabby cat, orange cat and white cat.
- **No dependencies**: No additional third-party library dependencies, run out of box.

## Quick Start

### Prerequisites

- Java 25

## Installation & Running

**Run the application** (spawns one random cat)

```bash
# bash / Linux / macOS
./gradlew run
```
```powershell
# Windows PowerShell
.\gradlew.bat run
```

## Spawning Cats

By default the app spawns **one random cat**. You can choose which cats appear, how many, and how
big they are by passing arguments.

**Available cat types:** `calico_cat` (tricolored), `grey_tabby_cat`, `orange_cat`, `white_cat`

### Via Gradle

Pass arguments with `--args` (use `.\gradlew.bat` on Windows):

```bash
# a single specific cat
./gradlew run --args="orange_cat"

# several cats at once — one per type listed (repeat a type to get duplicates)
./gradlew run --args="orange_cat calico_cat grey_tabby_cat"

# set the on-screen size in pixels with --size (default 100)
./gradlew run --args="--size 150 orange_cat white_cat"
```

### Via a built jar

```bash
./gradlew jar
java -jar build/libs/Catppuccino-1.0.jar orange_cat calico_cat
```

### Spawn automatically on startup

**Linux (freedesktop / GNOME / XFCE):** create `~/.config/autostart/catppuccino.desktop`:

```ini
[Desktop Entry]
Type=Application
Name=Catppuccino Cats
Exec=java -jar /absolute/path/to/Catppuccino-1.0.jar orange_cat calico_cat
Terminal=false
X-GNOME-Autostart-enabled=true
```

**Windows:** put a shortcut to `java -jar C:\path\to\Catppuccino-1.0.jar orange_cat calico_cat`
in the Startup folder (press <kbd>Win</kbd>+<kbd>R</kbd>, run `shell:startup`).

**macOS:** add the same `java -jar ...` command as a *Login Item* (System Settings → General →
Login Items), e.g. wrapped in a small `.command` script.

## How to Use

- **Interact**: Try clicking or dragging the kitten with your mouse to see its reactions!

- **Ignore It**: Don't worry, it will entertain itself happily without getting in your way.

Enjoy!

PS: Art resources copyright from Stardew Valley

![curled_1.png](src/main/resources/white_cat/curled/curled_1.png)![curled_1.png](src/main/resources/orange_cat/curled/curled_1.png)![curled_1.png](src/main/resources/grey_tabby_cat/curled/curled_1.png)![curled_1.png](src/main/resources/calico_cat/curled/curled_1.png)
