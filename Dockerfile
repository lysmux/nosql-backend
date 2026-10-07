FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies > /dev/null
COPY libs libs
COPY openapi/generated openapi/generated
COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
USER 1000:1000
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
