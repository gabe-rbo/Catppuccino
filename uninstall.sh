#!/usr/bin/env bash
# Remove the Catppuccino systemd user service installed by install.sh.
set -euo pipefail

SERVICE_NAME="catppuccino.service"
UNIT_PATH="${XDG_CONFIG_HOME:-$HOME/.config}/systemd/user/$SERVICE_NAME"

if systemctl --user list-unit-files "$SERVICE_NAME" >/dev/null 2>&1; then
    systemctl --user disable --now "$SERVICE_NAME" 2>/dev/null || true
fi
rm -f "$UNIT_PATH"
systemctl --user daemon-reload 2>/dev/null || true

echo "Catppuccino service removed. (Any running cats keep going until you Exit them from the tray.)"
