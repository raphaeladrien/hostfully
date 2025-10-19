FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS build

LABEL maintainer="Hostfully"

ENV APP_HOME=/app \
    GRADLE_OPTS=-Dorg.gradle.daemon=false

WORKDIR $APP_HOME

COPY gradle/ ./gradle/
COPY ["gradlew", "build.gradle", "settings.gradle", "$APP_HOME/"]

COPY src src

RUN chmod +x gradlew

RUN ./gradlew clean build --parallel

# Production image
FROM mcr.microsoft.com/openjdk/jdk:21-distroless AS booking

LABEL maintainer="Hostfully"

COPY --from=build /app/build/libs/booking.jar .

CMD ["-jar", "booking.jar"]