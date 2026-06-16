#!/bin/bash
# Update versionName and versionCode in app/build.gradle.kts
# Called by semantic-release during the prepare step.
#
# Usage: update-version.sh <semver> <major> <minor> <patch>
#
# Example: update-version.sh 1.2.3 1 2 3
#
# versionName = full semver string
# versionCode = monotonically increasing integer derived from (major * 1_000_000 + minor * 1_000 + patch)
# This scheme supports up to version 999.999.999 with no collisions.

set -euo pipefail

VERSION="$1"
MAJOR="${2:-0}"
MINOR="${3:-0}"
PATCH="${4:-0}"

BUILD_FILE="app/build.gradle.kts"

if [ ! -f "$BUILD_FILE" ]; then
  echo "ERROR: $BUILD_FILE not found. Run from project root."
  exit 1
fi

# versionCode = major * 1_000_000 + minor * 1_000 + patch
VERSION_CODE=$(( MAJOR * 1000000 + MINOR * 1000 + PATCH ))

sed -i "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$BUILD_FILE"
sed -i "s/versionCode = [0-9]*/versionCode = $VERSION_CODE/" "$BUILD_FILE"

echo "Updated $BUILD_FILE: versionName=$VERSION, versionCode=$VERSION_CODE"
