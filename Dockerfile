FROM gradle:jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
LABEL maintainer="bunq2025"

ARG JAR_FILE=build/libs/*.jar
COPY --from=build /app/${JAR_FILE} /app/app.jar

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port for the application
EXPOSE 8080

# Set environment variable to exclude the need for the installation.key file at runtime
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
