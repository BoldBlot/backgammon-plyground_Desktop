#!/usr/bin/env bash
# Builds the engines and their JNI library for the host at one CPU level, then runs the JVM unit tests against the
# real engines. Usage: BG_MARCH=x86-64|x86-64-v3 BUILD_DIR=<dir> scripts/host-tests.sh   (BG_MARCH defaults to x86-64)
# Leaves the library at $BUILD_DIR/libbgjni-$BG_MARCH.so.
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
MARCH="${BG_MARCH:-x86-64}"
BUILD="${BUILD_DIR:-$ROOT/build/engine-host-$MARCH}"
JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")}"
export JAVA_HOME

cmake -S "$ROOT/engine" -B "$BUILD" -G Ninja -DCMAKE_BUILD_TYPE=Release -DBG_MARCH="$MARCH" >/dev/null
cmake --build "$BUILD"
test -f "$BUILD/libbgjni-$MARCH.so" || { echo "libbgjni-$MARCH.so was not built (JAVA_HOME=$JAVA_HOME)" >&2; exit 1; }
"$ROOT/gradlew" -p "$ROOT" test -PbgHostLib="$BUILD/libbgjni-$MARCH.so" --console=plain
