FROM gcr.io/distroless/java21-debian13:nonroot

WORKDIR /app

COPY --chown=nonroot:nonroot target/mecanica-os-service-*.jar /app/app.jar

CMD [ "app.jar" ]
