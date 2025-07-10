# Stage 1: Build the application
FROM gradle:8.6-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
RUN gradle build --no-daemon -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Create directories for logs and uploads
RUN mkdir -p /home/appuser/event-ticketing/logs \
    && mkdir -p /home/appuser/event-ticketing/uploads

# Copy the built artifact from the build stage
COPY --from=build --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 