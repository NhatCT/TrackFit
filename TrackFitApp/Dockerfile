# ===== Stage 1: Build WAR =====
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -q -U -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package
# kỳ vọng tạo ra target/TrackFitApp-1.0-SNAPSHOT.war (tên theo <artifactId>-<version>.war)

# ===== Stage 2: Run on Tomcat =====
# Tomcat 11 chạy Java 17+, hỗ trợ Jakarta Servlet 6.x (phù hợp Spring 6/Jakarta 10+)
FROM tomcat:11.0-jre17-temurin
# (tuỳ chọn) gỡ webapps mặc định cho gọn
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR và đặt tên TrackFit.war để context path = /TrackFit
COPY --from=builder /build/target/*.war /usr/local/tomcat/webapps/TrackFit.war

EXPOSE 8080
CMD ["catalina.sh","run"]
