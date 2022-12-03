FROM openjdk:16-jdk
EXPOSE 8080:8080

RUN mkdir -p /sspu/search-manager
WORKDIR /sspu/search-manager

COPY build.gradle gradle gradle.properties gradlew settings.gradle ./
COPY libraries ./libraries
COPY src ./src
COPY gradle ./gradle

RUN touch .env
RUN chmod +x gradlew
RUN ./gradlew build && ./gradlew jar
RUN mv build/libs/searchmanager* ./search-manager.jar
RUN rm -rf build .gradle/ gradle/ build.gradle gradle.properties gradlew settings.gradle

ENTRYPOINT ["java", "-jar", "search-manager.jar"]

