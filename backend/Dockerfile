# Production image of the backend: built from the sources, run on a JRE (compose.prod.yaml, docs/deploy.md).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
# .mvn carries the wrapper and the JVM options Error Prone needs to compile.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests && cp target/backend-*.jar /build/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
