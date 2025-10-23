# ========== 1️⃣ СБОРКА JAR через Maven ==========
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (чтобы кешировать)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Копируем исходники и собираем проект
COPY src ./src
RUN mvn -B clean package -DskipTests

# ========== 2️⃣ ЗАПУСК ГОТОВОГО ПРИЛОЖЕНИЯ ==========
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Устанавливаем порт
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

# Запуск Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]

#docker build -t user-service:1.0 .
