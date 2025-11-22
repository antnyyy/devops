#!/usr/bin/env bash
set -euo pipefail

# Helper to run Maven inside a Maven image that uses Eclipse Temurin JDK 17.
# Usage: ./scripts/mvn.sh package -DskipTests
#
# Real-world scenario: useful for local development and in CI environments
# where the host does not have Maven/JDK installed; it ensures consistent
# build tooling by running Maven inside a pinned container image.

IMAGE="maven:3.9.6-eclipse-temurin-17"

exec docker run --rm -v "$PWD":/workspace -w /workspace -u $(id -u):$(id -g) $IMAGE mvn "$@"
