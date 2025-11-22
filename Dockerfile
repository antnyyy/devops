FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Multi-stage build: useful in real CI/CD pipelines where the build
# environment (heavy Maven image) is used to compile and assemble the
# application, while a smaller, patched runtime image is used for the
# final artifact to reduce attack surface and image size.
#
# Typical production flow: CI builds the assembly JAR in the builder
# stage, then the runtime image contains only the single runnable JAR.

# copy only what we need for a standard maven build
COPY pom.xml ./
COPY src ./src

# build the assembly jar (assembly plugin configured in pom.xml)
RUN mvn -DskipTests package

FROM amazoncorretto:17
# copy the assembled "jar-with-dependencies" from the builder stage
COPY --from=builder /workspace/target/seMethods-0.1.0.2-jar-with-dependencies.jar /tmp/
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "seMethods-0.1.0.2-jar-with-dependencies.jar"]
