#!/usr/bin/env bash
# Takes the app's screenshots into OUT_DIR (default build/screenshots), from a fresh data folder, so they look the same
# every time. Drives the real window on the X display with scripts/ui.sh (xdotool): it needs a display, and the
# coordinates are for a 1100x713 window. The game's opening roll is random; the other screens don't change.
set -euo pipefail
echo "screenshots.sh still clicks the coordinates of the 0.1.0 screens: it needs rewriting for the 0.2.0 layout." >&2
exit 1
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT="${1:-$ROOT/build/screenshots}"
mkdir -p "$OUT"
export XDG_DATA_HOME="$OUT/.data"
rm -rf "$XDG_DATA_HOME"
export UI_DIRECT=1 UI_LOG="$OUT/app.log"
cd "$ROOT"
./gradlew -q prepareAppResources printClasspath > build/classpath.txt
UI=scripts/ui.sh
shot() { sleep "${2:-1.5}"; $UI shot "$OUT/$1.png"; echo "$1"; }
back() { $UI key Escape; sleep 1; }

$UI start
$UI size 1100 713; sleep 1.5
shot 01-welcome
$UI click 70 669; shot 02-home
$UI click 812 457; shot 03-settings; back
$UI click 812 567; shot 04-about; back
$UI click 812 225; shot 05-analyze-position; back
$UI click 812 341; shot 06-statistics; back
# A match file sent to the running copy (also tests the single-instance hand-over); the 7-point match from the tests.
java --enable-native-access=ALL-UNNAMED -Dcompose.application.resources.dir="$ROOT/build/compose/tmp/prepareAppResources" \
  -cp "$(cat build/classpath.txt)" com.boldblot.backgammonplyground.MainKt "$ROOT/src/test/resources/mat/gnubg-match-7pt.mat"
shot 07-import-match 2
$UI click 770 555; shot 08-match-library 8
$UI click 300 172; shot 09-review 5
back; back
$UI click 812 167; shot 10-new-match
$UI click 1033 53; shot 11-game 5
$UI stop
echo "screenshots in $OUT"
