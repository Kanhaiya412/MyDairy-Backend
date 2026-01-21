# # ---------- Stage 1: Build ----------
# FROM maven:3.9.9-eclipse-temurin-17 AS builder
# WORKDIR /build
# COPY pom.xml .
# RUN mvn dependency:go-offline
# COPY src ./src
# RUN mvn package -DskipTests
#
# # ---------- Stage 2: Runtime ----------
# FROM eclipse-temurin:17-jdk-jammy
# WORKDIR /app
# COPY --from=builder /build/target/*.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# 🔥 Enable Java Remote Debugging on port 5005
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"

# Copy JAR
COPY --from=builder /build/target/*.jar app.jar

# Expose API + Debug Port
EXPOSE 8080
EXPOSE 5005

ENTRYPOINT ["java", "-jar", "app.jar"]
