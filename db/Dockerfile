# This image is intended for integration testing and local development.
# It initialises a MySQL server and imports the provided world dataset on
# first startup (files under /docker-entrypoint-initdb.d are executed by
# the official MySQL entrypoint).
FROM mysql/mysql-server:latest
WORKDIR /tmp
COPY world-db/world.sql /tmp/
COPY world-db/world.sql /docker-entrypoint-initdb.d

# Default credentials used for the CI/test environment. Do not embed
# production secrets in Dockerfiles; use environment variables or secret
# managers instead.
ENV MYSQL_ROOT_PASSWORD=example
ENV MYSQL_ROOT_HOST=%