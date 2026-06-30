FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -q -U -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM tomcat:11.0-jre17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /build/target/*.war /usr/local/tomcat/webapps/TrackFit.war

EXPOSE 8080
CMD ["catalina.sh","run"]
