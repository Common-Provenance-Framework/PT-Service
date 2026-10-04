FROM eclipse-temurin:25-jdk@sha256:dcf835e52330939b6c9f90ecab8aafcbcaa8fbf48423db44de884cf978c10144 AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:26-jre-alpine-3.24@sha256:9eedff2367194d11eddd6f14101b444945a708c986270cd5716b934596ba3a31 AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
