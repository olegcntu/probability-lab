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
      org.opencontainers.image.authors="Красавін Олег Олександрович (2 курс магістратури, група 51-фмт, НУЧК імені Т. Г. Шевченка)"
WORKDIR /app
# Безкоштовні хостинги дають близько 512 МБ пам'яті й невелику частку процесора – обмежуємо купу
ENV JAVA_OPTS="-Xmx300m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
COPY --from=build /app/target/probability-lab.jar probability-lab.jar
# Розпакований jar і архів класів (CDS) майже вдвічі зменшують процесорний час запуску,
# тож після «сну» безкоштовного сервера сторінка відкривається швидше
RUN java -Djarmode=tools -jar probability-lab.jar extract --destination application \
 && rm probability-lab.jar \
 && cd application \
 && java $JAVA_OPTS -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh -jar probability-lab.jar
EXPOSE 8080
# Хостинг (наприклад, Render) передає порт у змінній PORT
WORKDIR /app/application
CMD ["sh", "-c", "exec java $JAVA_OPTS -XX:SharedArchiveFile=application.jsa -Dserver.port=${PORT:-8080} -jar probability-lab.jar"]
