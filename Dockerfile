# ---------- Этап 1: сборка (Maven + JDK 21) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Сначала копируем только pom.xml, чтобы зависимости кэшировались в слое Docker
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Исходники и сборка исполняемого jar
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- Этап 2: запуск (только JRE, маленький образ) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/gamestudio-*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# MaxRAMPercentage - ограничить heap (на бесплатном тарифе ~512 MB RAM на инстанс)
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -jar app.jar --server.port=${PORT:-8080}"]
