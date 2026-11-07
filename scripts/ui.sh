#!/usr/bin/env bash
# Drives the running desktop app on the X display, for looking at it and for scripted screenshots.
#   scripts/ui.sh start [FILES]    start `./gradlew run` in the background (UI_DIRECT=1: java directly) and wait for the window
#   scripts/ui.sh shot FILE.png    capture the window
#   scripts/ui.sh click X Y        click at window coordinates (the window's top-left is 0,0)
#   scripts/ui.sh key KEY...       send keys (xdotool names: Return, Escape, ctrl+o, Left ...)
#   scripts/ui.sh type TEXT        type text
#   scripts/ui.sh size W H         resize the window
#   scripts/ui.sh stop             close the app (by its window's process)
# Needs a display (no xvfb-run here) and xdotool, xwd, python3 with PIL.
set -uo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
LOG="${UI_LOG:-/tmp/plyground-ui.log}"
# UI_WIN: the title of another window to drive (Match Library, Preferences...).
win() { xdotool search --onlyvisible --name "${UI_WIN:-^Backgammon PlyGround [0-9]}" 2>/dev/null | tail -1; }
case "${1:-}" in
  start)
    if [ -n "${UI_DIRECT:-}" ]; then
      # Without Gradle (faster; run `./gradlew -q printClasspath > build/classpath.txt` and `./gradlew prepareAppResources` first).
      (cd "$ROOT" && nohup java --enable-native-access=ALL-UNNAMED -Dcompose.application.resources.dir="$ROOT/build/compose/tmp/prepareAppResources" \
        -cp "$(cat build/classpath.txt)" com.boldblot.backgammonplyground.MainKt "${@:2}" > "$LOG" 2>&1 &)
    else
      (cd "$ROOT" && nohup ./gradlew run --console=plain > "$LOG" 2>&1 &)
    fi
    for _ in $(seq 1 120); do [ -n "$(win)" ] && { sleep 3; echo "window $(win)"; exit 0; }; sleep 1; done
    echo "no window; see $LOG" >&2; exit 1 ;;
  shot)
    W=$(win); [ -n "$W" ] || { echo "no window" >&2; exit 1; }
    xwd -id "$W" -silent -out "$2.xwd" && python3 - "$2.xwd" "$2" <<'P'
import struct, sys
from PIL import Image
d = open(sys.argv[1], 'rb').read()
f = struct.unpack('>25I', d[:100])
hs, w, h, bo, bpl, ncol = f[0], f[4], f[5], f[7], f[12], f[19]
pix = d[hs + ncol * 12:]
Image.frombuffer('RGBA', (w, h), pix, 'raw', 'BGRA' if bo == 0 else 'ARGB', bpl, 1).convert('RGB').save(sys.argv[2])
P
    rm -f "$2.xwd" ;;
  click)  xdotool mousemove --window "$(win)" "$2" "$3" click 1 ;;
  key)    shift; xdotool windowactivate --sync "$(win)" 2>/dev/null; sleep 0.2; xdotool key "$@" ;;
  type)   xdotool windowactivate --sync "$(win)" 2>/dev/null; xdotool type "$2" ;;
  size)   xdotool windowsize "$(win)" "$2" "$3" ;;
  stop)
    PID=$(xdotool getwindowpid "$(win)" 2>/dev/null); [ -n "$PID" ] && kill "$PID"; sleep 2 ;;
  *) sed -n 2,12p "$0"; exit 2 ;;
esac
