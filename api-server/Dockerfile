FROM maven:3.9.1-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:resolve dependency:resolve-plugins
COPY src/ /app/src/
RUN mvn -B clean package -DskipTests
#
#FROM eclipse-temurin:17-jdk-alpine
ENTRYPOINT ["java","-jar","/app/target/api-server-1.0.0.jar"]
#ENTRYPOINT ["tail","-f","/dev/null"]
