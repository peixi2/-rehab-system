FROM node:22-alpine AS frontend-build
WORKDIR /build/frontend

COPY frontend/package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-21-alpine AS backend-build
WORKDIR /build
COPY pom.xml ./
COPY maven-settings.xml /root/.m2/settings.xml
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B -q
COPY src ./src
COPY seed.sql scale_seed.sql ./
COPY --from=frontend-build /build/frontend/dist/ ./src/main/resources/static/
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S rehab && adduser -S rehab -G rehab
COPY --from=backend-build /build/target/rehab-system-1.0.0.jar /app/app.jar

USER rehab
EXPOSE 8080

ENV TZ="Asia/Shanghai"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseG1GC -Duser.timezone=Asia/Shanghai"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
