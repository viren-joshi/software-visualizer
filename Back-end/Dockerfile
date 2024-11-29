
FROM amazoncorretto:17-al2023-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the Maven build directory into the container
COPY target/*.jar app.jar
COPY /home/student/firebase-config/firebase-config.json firebase-config.json

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-config.json

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
