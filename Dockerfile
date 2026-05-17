# build app
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app

# down dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# build jar
COPY src ./src
RUN mvn clean package -DskipTests

# run app
FROM eclipse-temurin:22-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]