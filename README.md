# Main build status
![workflow](https://github.com/antnyyy/devops/actions/workflows/main.yml/badge.svg)
# Develop build status
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/antnyyy/devops/main.yml)
# License
[![LICENSE](https://img.shields.io/github/license/antnyyy/devops.svg?style=flat-square)](https://github.com/antnyyy/devops/blob/master/LICENSE)
# Release
[![Releases](https://img.shields.io/github/release/antnyyy/devops/all.svg?style=flat-square)](https://github.com/antnyyy/devops/releases)
# Codecov
[![codecov](https://codecov.io/gh/antnyyy/devops/branch/main/graph/badge.svg)](https://codecov.io/gh/antnyyy/devops) <br>
[![Coverage Sunburst](https://codecov.io/gh/antnyyy/devops/branch/main/graphs/sunburst.svg)](https://codecov.io/gh/antnyyy/devops)



docker compose up -d db
docker compose run --rm app

## Building locally if your host doesn't have JDK 17

The project requires Java 17. If your local environment (or dev container) uses an older JDK, you can run Maven inside a Docker image that provides JDK 17.

Use the included helper script:

```bash
# build the project using Maven + JDK17 in Docker
./scripts/mvn.sh -DskipTests package

# run with docker compose (the Dockerfile is multistage and will build the jar if needed)
docker compose build --no-cache app
docker compose run --rm app
```

Or run Maven directly with:

```bash
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.6-eclipse-temurin-17 mvn -DskipTests package
```
