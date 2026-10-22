# ---- build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70.0"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Writes the JWT private key from env (Render has no persistent disk),
# then starts the app. exec passes signals (SIGTERM) to the JVM.
ENTRYPOINT ["sh","-c","if [ -n \"$JWT_PRIVATE_KEY\" ]; then mkdir -p config/keys && printf '%s' \"$JWT_PRIVATE_KEY\" > config/keys/private.pem; fi && exec java $JAVA_OPTS -jar app.jar"]
