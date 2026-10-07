FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre

ARG OTEL_VERSION=2.12.0

WORKDIR /app

RUN mkdir -p /otel
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /workspace/target/*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
