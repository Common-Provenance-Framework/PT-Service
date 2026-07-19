FROM eclipse-temurin:23-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:23-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
