# Base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper & project files
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the code
COPY src ./src

# Package the app
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/vegetable-shop-backend-1.0.0.jar"]
