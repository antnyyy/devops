# Use the official Amazon Corretto 17 runtime
FROM amazoncorretto:17-alpine-jdk

# Copy the clean, correctly-named fat JAR
COPY ./target/seMethods-jar-with-dependencies.jar /app/app.jar

# Set working directory
WORKDIR /app

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]