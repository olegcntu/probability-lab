# Збирання: Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Запуск: лише JRE 17
FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.title="Лабораторія ймовірностей" \
      org.opencontainers.image.authors="Красавін Олег Олександрович (3 курс, група 51-фмт, НУЧК імені Т. Г. Шевченка)"
WORKDIR /app
COPY --from=build /app/target/probability-lab.jar app.jar
# Безкоштовні хостинги дають близько 512 МБ пам'яті — обмежуємо купу
ENV JAVA_OPTS="-Xmx300m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
# Хостинг (наприклад, Render) передає порт у змінній PORT
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
