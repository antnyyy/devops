FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

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
