#!/usr/bin/env bash
# Builds the Debian package: build/dist/backgammon-plyground_<version>_amd64.deb
# Gradle's packageDeb names the menu entry after the app's folder, "BackgammonPlyGround"; this puts the app's real name in it and
# ties the entry to the window, so the taskbar shows the right icon.
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT"
VERSION=$(sed -n 's/^desktopVersion=//p' gradle.properties)
./gradlew packageDeb --console=plain > build/package-deb.log 2>&1 || { tail -30 build/package-deb.log; exit 1; }
DEB="build/compose/binaries/main/deb/backgammon-plyground_${VERSION}_amd64.deb"
WORK=build/dist/deb-work
OUT="build/dist/backgammon-plyground_${VERSION}_amd64.deb"
mkdir -p build/dist
rm -rf "$WORK" "$OUT"
fakeroot sh -c "
  dpkg-deb -R '$DEB' '$WORK'
  DESKTOP='$WORK/opt/backgammon-plyground/lib/backgammon-plyground-BackgammonPlyGround.desktop'
  sed -i 's/^Name=.*/Name=Backgammon PlyGround/; s/^Categories=.*/Categories=Game;BoardGame;/; /^MimeType=/d' \"\$DESKTOP\"
  echo 'StartupWMClass=com-boldblot-backgammonplyground-MainKt' >> \"\$DESKTOP\"
  dpkg-deb -b -Zxz '$WORK' '$OUT' > /dev/null
"
rm -rf "$WORK"
ls -l "$OUT"
