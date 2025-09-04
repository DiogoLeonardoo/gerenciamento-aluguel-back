# Stage 1: Build do JAR
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Imagem final leve
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/project-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
