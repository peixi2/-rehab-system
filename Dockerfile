# 后端镜像：多阶段构建，容器内 Maven 编译，无需本地 JDK/Maven
FROM maven:3.9-eclipse-temurin-19 AS build
WORKDIR /build
COPY pom.xml .
COPY maven-settings.xml /root/.m2/settings.xml
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B -q

FROM eclipse-temurin:19-jre
WORKDIR /app
COPY --from=build /build/target/rehab-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
