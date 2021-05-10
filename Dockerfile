# Docker file for two phase build
# Phase 1 - Build the application .jar file and name it builder
FROM openjdk:11.0-jdk-slim as builder
RUN apt-get update && apt-get install -y dos2unix

ENV SRC_HOME=/app
WORKDIR $SRC_HOME
COPY build.gradle settings.gradle gradlew $SRC_HOME/
COPY gradle $SRC_HOME/gradle
RUN dos2unix gradlew
RUN sh gradlew dependencies

COPY src $SRC_HOME/src
RUN sh gradlew build

# Phase 2 - Build container with runtime only to use .jar file within
FROM openjdk:11.0-jre-slim
ENV SRC_HOME=/app
WORKDIR /app
# Copy .jar file (aka, builder)
COPY --from=builder $SRC_HOME/build/libs/*.jar app.jar
CMD [ "sh", "-c", "java -Xmx300m -Xss512k -Dserver.port=$PORT -jar /app/app.jar" ]
EXPOSE 8080
