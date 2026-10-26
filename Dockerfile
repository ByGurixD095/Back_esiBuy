FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/esiBuy-0.0.1-SNAPSHOT.war /app/app.war
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
