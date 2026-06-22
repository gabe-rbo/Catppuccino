#!/usr/bin/env bash
# Build Catppuccino and install it as an always-on system-tray app via a systemd *user* service.
# No root required. The tray icon appears immediately and on every login (with no cats — spawn them
# from the tray menu), and auto-restarts if it crashes.
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_NAME="catppuccino.service"
UNIT_DIR="${XDG_CONFIG_HOME:-$HOME/.config}/systemd/user"
UNIT_PATH="$UNIT_DIR/$SERVICE_NAME"

if ! command -v systemctl >/dev/null 2>&1 || ! systemctl --user show-environment >/dev/null 2>&1; then
    echo "error: this installer needs a systemd --user instance (Linux desktop session)." >&2
    echo "On Windows/macOS see the README startup instructions instead." >&2
    exit 1
fi

echo ">> Building the jar…"
( cd "$REPO_DIR" && ./gradlew jar )

JAR="$(ls "$REPO_DIR"/build/libs/Catppuccino-*.jar 2>/dev/null | head -n1 || true)"
if [[ -z "${JAR:-}" || ! -f "$JAR" ]]; then
    echo "error: built jar not found under $REPO_DIR/build/libs/" >&2
    exit 1
fi

JAVA="$(command -v java || true)"
if [[ -z "$JAVA" ]]; then
    echo "error: no 'java' on PATH. Install JDK 25 (e.g. 'sudo apt install openjdk-25-jdk')." >&2
    exit 1
fi

echo ">> Writing $UNIT_PATH"
mkdir -p "$UNIT_DIR"
cat > "$UNIT_PATH" <<EOF
[Unit]
Description=Catppuccino desktop cats (system tray)
After=default.target

[Service]
Type=simple
Environment=DISPLAY=:0
Environment=XAUTHORITY=%h/.Xauthority
ExecStart=$JAVA -jar $JAR
Restart=on-failure
RestartSec=3

[Install]
WantedBy=default.target
EOF

# Seed an empty startup routine so the default launch is tray-only (no cats).
# Edit it later from the tray's "Startup" menu. Don't clobber an existing routine.
ROUTINE="${XDG_CONFIG_HOME:-$HOME/.config}/catppuccino/startup.properties"
if [[ ! -f "$ROUTINE" ]]; then
    echo ">> Seeding empty startup routine $ROUTINE (tray only; configure from the tray menu)"
    mkdir -p "$(dirname "$ROUTINE")"
    printf 'size=100\ncats=\n' > "$ROUTINE"
fi

# Retire the old XDG autostart entry (if any) so login doesn't double-launch the cats.
OLD_AUTOSTART="${XDG_CONFIG_HOME:-$HOME/.config}/autostart/catppuccino.desktop"
if [[ -f "$OLD_AUTOSTART" ]]; then
    echo ">> Removing old autostart entry $OLD_AUTOSTART (replaced by the systemd service)"
    rm -f "$OLD_AUTOSTART"
fi

echo ">> Enabling and starting the service…"
systemctl --user daemon-reload
# Give the immediate start the real session DISPLAY/XAUTHORITY (best-effort).
systemctl --user import-environment DISPLAY XAUTHORITY 2>/dev/null || true
systemctl --user enable --now "$SERVICE_NAME"

echo
echo "Done. The Catppuccino tray icon should now be present (no cats yet — use the tray's Spawn menu)."
echo "  status:  systemctl --user status catppuccino"
echo "  logs:    journalctl --user -u catppuccino -f"
echo "  remove:  ./uninstall.sh"
echo "  cats-at-login: configure them from the tray's \"Startup\" menu (persists to $ROUTINE)"
