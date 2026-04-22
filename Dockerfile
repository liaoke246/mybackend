FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn ./.mvn
COPY mvnw ./
COPY pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src ./src

ARG SKIP_TESTS=true
RUN if [ "$SKIP_TESTS" = "true" ]; then \
        ./mvnw -B -ntp clean package -Dmaven.test.skip=true; \
    else \
        ./mvnw -B -ntp clean package; \
    fi

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/demo-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
