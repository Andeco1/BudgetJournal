# Build stage
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Run stage
FROM tomcat:9.0-jdk17-openjdk
WORKDIR /usr/local/tomcat

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Create directory for logs
RUN mkdir -p /usr/local/tomcat/logs

# Set environment variables
ENV DB_URL=jdbc:postgresql://db:5432/budget_journal
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV DB_SCHEMA=budget_journal
ENV JAVA_OPTS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8"

# Expose port 8080
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"] 