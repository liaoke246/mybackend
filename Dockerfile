FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src

ARG SKIP_TESTS=true
RUN if [ "$SKIP_TESTS" = "true" ]; then \
        mvn -B -ntp clean package -Dmaven.test.skip=true; \
    else \
        mvn -B -ntp clean package; \
    fi

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/target/demo-*.jar /app/app.jar
COPY .env /app/.env

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
