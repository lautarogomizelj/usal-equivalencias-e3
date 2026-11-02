FROM maven:3.9-eclipse-temurin-17 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

FROM deps AS test
COPY src ./src
CMD ["mvn", "-B", "test"]

FROM deps AS build
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre AS app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
