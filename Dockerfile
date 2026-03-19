FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew && \
	if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then echo 'ERROR: gradle-wrapper.jar missing. Please add it to gradle/wrapper.' && exit 1; fi && \
	./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

