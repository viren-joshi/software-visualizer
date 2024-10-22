
FROM amazoncorretto:17-al2023-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the Maven build directory into the container
COPY target/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
