FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /opt/tk-adpro
COPY . .
RUN ./gradlew clean assemble

FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/tk-adpro
COPY --from=builder /opt/tk-adpro/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]