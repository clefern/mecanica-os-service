FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# GITHUB_TOKEN necessário para baixar mecanica-shared-kernel do GitHub Packages
ARG GITHUB_TOKEN
RUN mvn --batch-mode package -DskipTests -Dgithub.token=${GITHUB_TOKEN}

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/mecanica-os-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
