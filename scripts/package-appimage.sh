#!/usr/bin/env bash
# Builds the single-file Linux package: build/dist/Backgammon_PlyGround-<version>-x86_64.AppImage
# Gradle's packageAppImage only makes the app folder, with its own Java; appimagetool turns that into the one file.
#   APPIMAGETOOL=/path/to/appimagetool scripts/package-appimage.sh
# appimagetool: https://github.com/AppImage/appimagetool/releases
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
TOOL="${APPIMAGETOOL:?set APPIMAGETOOL to the path of appimagetool}"
cd "$ROOT"
VERSION=$(sed -n 's/^desktopVersion=//p' gradle.properties)
./gradlew packageAppImage --console=plain > build/package-appimage.log 2>&1 || { tail -30 build/package-appimage.log; exit 1; }
DIR=build/dist/AppDir
rm -rf "$DIR"
mkdir -p "$DIR"
cp -a build/compose/binaries/main/app/BackgammonPlyGround "$DIR/BackgammonPlyGround"
cp src/main/composeResources/drawable/plyground_logo.png "$DIR/backgammon-plyground.png"
ln -s backgammon-plyground.png "$DIR/.DirIcon"
cat > "$DIR/AppRun" <<'RUN'
#!/bin/sh
HERE=$(dirname "$(readlink -f "$0")")
exec "$HERE/BackgammonPlyGround/bin/BackgammonPlyGround" "$@"
RUN
chmod +x "$DIR/AppRun"
cat > "$DIR/backgammon-plyground.desktop" <<DESKTOP
[Desktop Entry]
Type=Application
Name=Backgammon PlyGround
Comment=Play a world-class backgammon engine, review every game, learn from every mistake
Exec=AppRun %F
Icon=backgammon-plyground
Terminal=false
Categories=Game;BoardGame;
StartupWMClass=com-boldblot-backgammonplyground-MainKt
DESKTOP
desktop-file-validate "$DIR/backgammon-plyground.desktop"
OUT="build/dist/Backgammon_PlyGround-$VERSION-x86_64.AppImage"
rm -f "$OUT"
ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "$TOOL" --no-appstream "$DIR" "$OUT" > build/dist/appimagetool.log 2>&1 || { tail -30 build/dist/appimagetool.log; exit 1; }
ls -l "$OUT"
