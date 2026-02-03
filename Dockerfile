# 第一階段：使用 Maven 編譯 (這部分沒變)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# 第二階段：使用 Eclipse Temurin 執行 (這裡換成了新版穩定的基底)
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]