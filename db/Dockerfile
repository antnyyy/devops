# This image is used for integration testing and local development.
# Real-world scenario context:
# - The container initialises a MySQL instance and loads the canonical
#   `world` dataset. CI can use this image (via docker-compose or GitHub
#   Actions services) to run integration tests against a known dataset.
FROM mysql/mysql-server:latest
WORKDIR /tmp

# Copy the seed SQL file and ensure it's executed by MySQL on first start.
# The official MySQL images execute files placed under
# `/docker-entrypoint-initdb.d` during initialisation.
COPY world-db/world.sql /tmp/
COPY world-db/world.sql /docker-entrypoint-initdb.d

# Default credentials used for CI integration tests. In production these
# would be replaced with secure secrets and not baked into the image.
ENV MYSQL_ROOT_PASSWORD=example
ENV MYSQL_ROOT_HOST=%