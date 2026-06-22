# 🐱 Catppuccino

> **Forked from [DongH-Chen/Catppuccino](https://github.com/DongH-Chen/Catppuccino).**
> This fork adds a full system-tray control panel, named cats, a configurable startup routine, and a
> one-command always-on install for Linux.

Little pixel cats that live on your desktop. They wander around, stretch, nap, and generally keep you
company while you work — and a tray icon lets you run the whole thing without ever touching a terminal.

![preview](doc/video.gif)

## What this fork adds

- **System-tray control panel** — one icon to rule the cats: put them all to sleep, show/edit their
  names, change their size, and spawn or remove individual cats. The icon stays put even with zero cats.
- **Named cats** — every cat gets a name (random, or one you choose) shown on a little tag above it.
- **Configurable startup routine** — pick exactly which cats (color + name) spawn when the app
  launches; it's saved to disk and editable from the tray's **Startup** menu.
- **Always-on install** — `./install.sh` registers a systemd *user* service so the tray is there on
  every login and auto-restarts if it ever crashes. No root, no packaging.

## Requirements

- **Java 25** (JRE/JDK). On Debian/Ubuntu/Mint: `sudo apt install openjdk-25-jdk`.

## Quick start

```bash
# Linux / macOS
./gradlew run

# Windows
.\gradlew.bat run
```

This spawns one random cat and adds the Catppuccino icon to your system tray.

## Using the tray menu

Click the tray icon for the control panel:

| Item | What it does |
|------|--------------|
| **Sleep (all cats)** | Curls every cat up with a 💤 bubble and stops them roaming. Untick to wake. |
| **Show names** | Toggles a name tag above each cat. |
| **Size** | Resize every cat to a preset (75–200 px). |
| **Spawn** | Add a cat right now — pick a color or *Random*. |
| **Rename / Remove** | Rename or remove a specific live cat by name (*Remove all* keeps the tray icon). |
| **Startup** | Edit the routine that runs at launch — add/rename/remove cats, or **Use current on-screen cats** to snapshot what you have now. |

> There is intentionally **no "Exit"** item — this is meant to be an always-on tray app, so it can't
> be killed by a stray click. To stop it, see [Stopping it](#stopping-it).

## Command-line options

Cats and options can also be passed as arguments (handy for `./gradlew run --args="…"` or a jar):

```bash
# specific cats (one per word; repeat a type for duplicates)
orange_cat calico_cat grey_tabby_cat

# name a cat with type:name
orange_cat:cebola calico_cat:kamala

# on-screen size in pixels (default 100)
--size 150 orange_cat white_cat

# start with only the tray icon, no cats
--no-cats
```

**Cat types:** `calico_cat` (tricolored), `grey_tabby_cat`, `orange_cat`, `white_cat`.

When no cat arguments are given, the app uses your saved **startup routine** (see below); if there's
no routine yet, it spawns one random cat.

## Always-on tray (Linux, systemd) — recommended

Run the installer once. It builds the jar and registers a systemd **user** service that starts the
tray at every login and restarts it on crash:

```bash
./install.sh
```

Out of the box the service launches **tray-only (no cats)** — open the tray's **Startup** menu to
choose which cats spawn at login (it's saved to `~/.config/catppuccino/startup.properties`). Useful
commands:

```bash
systemctl --user status catppuccino     # is it running?
journalctl --user -u catppuccino -f      # live logs
./uninstall.sh                           # remove the service
```

A graphical login session is required (the tray needs an X display), so the service starts at login
rather than at boot.

### Startup on Windows / macOS

- **Windows:** put a shortcut to `java -jar C:\path\to\Catppuccino-1.0.jar --no-cats` in the Startup
  folder (<kbd>Win</kbd>+<kbd>R</kbd> → `shell:startup`).
- **macOS:** add `java -jar /path/to/Catppuccino-1.0.jar --no-cats` as a *Login Item* (System
  Settings → General → Login Items), e.g. wrapped in a small `.command` script.

## Building a jar

```bash
./gradlew jar
java -jar build/libs/Catppuccino-1.0.jar orange_cat calico_cat
```

## Stopping it

Since there's no Exit menu item:

- Installed as the service: `systemctl --user stop catppuccino` (or `./uninstall.sh` to remove it).
- Launched manually: close the terminal / `Ctrl+C`, or `kill` the `java` process.

## Interacting with the cats

- **Drag** a cat to pick it up; it'll flop back down when you let go.
- **Click** a cat for a little ❤️.
- Otherwise just ignore them — they entertain themselves and stay out of your way.

## Credits

- Original project: **[DongH-Chen/Catppuccino](https://github.com/DongH-Chen/Catppuccino)** — this is
  a fork.
- Cat sprite art: from **Stardew Valley**.
