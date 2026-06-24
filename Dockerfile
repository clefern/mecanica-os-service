FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_TOKEN
WORKDIR /app

# Inline settings.xml so Maven can authenticate to GitHub Packages
RUN mkdir -p /root/.m2 && \
    printf '<settings><servers><server><id>github</id><username>x-access-token</username><password>%s</password></server></servers></settings>' \
    "${GITHUB_TOKEN}" > /root/.m2/settings.xml

COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn --batch-mode package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/mecanica-os-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
