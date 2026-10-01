#!/usr/bin/env sh
#
# Gradle start up script
#
DIRNAME=$(dirname "$0")
# Use system gradle if available, else download
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle not found, trying wrapper..."
  exec java -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
fi
