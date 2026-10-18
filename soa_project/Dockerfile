ARG MODULE

FROM maven:3.9.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY ${MODULE}/pom.xml ${MODULE}/pom.xml
COPY ${MODULE}/src ${MODULE}/src

RUN mvn --batch-mode -f "${MODULE}/pom.xml" -DskipTests package \
    && mkdir -p /app \
    && cp "${MODULE}"/target/*.jar /app/app.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/app.jar ./app.jar
RUN mkdir -p /app/data \
    && chown -R 10001:0 /app \
    && chmod -R g=u /app
USER 10001:0

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
