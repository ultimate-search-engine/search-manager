FROM openjdk:8-jdk

RUN apt-get update -q && apt-get upgrade -y

EXPOSE 8080:8080

RUN mkdir -p /sspu/search-manager
WORKDIR /sspu/search-manager

COPY build.gradle gradle gradle.properties gradlew settings.gradle ./
COPY src ./src
COPY gradle ./gradle

RUN ./gradlew build
RUN mv build/libs/searchmanager* ./search-manager.jar
RUN rm -rf build .gradle/ gradle/ build.gradle gradle.properties gradlew settings.gradle

ENTRYPOINT ["java", "-jar", "search-manager.jar"]

