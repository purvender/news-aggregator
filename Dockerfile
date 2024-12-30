# Use an official OpenJDK 21 runtime as a parent image
FROM openjdk:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR to the container
COPY target/news-aggregator-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Define environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
