# 后端镜像：本地 mvn package 打好 jar 后直接拷入镜像
FROM eclipse-temurin:19-jre
WORKDIR /app
COPY target/rehab-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
