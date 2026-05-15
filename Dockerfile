# 多阶段构建：前端 + 后端

# --------- 前端构建阶段 ---------
FROM node:22.21.0 AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ ./
RUN npm run build

# --------- 后端构建阶段 ---------
FROM eclipse-temurin:17-jdk AS backend-build
WORKDIR /app
COPY . .
# 先将前端产物放入 Spring Boot 静态目录，再打包进 jar
COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
RUN ./mvnw clean package -DskipTests

# --------- 生产镜像 ---------
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]