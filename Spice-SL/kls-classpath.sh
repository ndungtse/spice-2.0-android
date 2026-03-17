#!/usr/bin/env sh
# Outputs classpath for Kotlin Language Server (fwcd). Run from project root.
cd "$(dirname "$0")"
./gradlew :app:klsClasspath -q 2>/dev/null || true
