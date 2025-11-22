#!/usr/bin/env bash
set -euo pipefail

# Helper to run Maven inside a Maven image that uses Eclipse Temurin JDK 17.
# Usage: ./scripts/mvn.sh package -DskipTests

IMAGE="maven:3.9.6-eclipse-temurin-17"

exec docker run --rm -v "$PWD":/workspace -w /workspace -u $(id -u):$(id -g) $IMAGE mvn "$@"
