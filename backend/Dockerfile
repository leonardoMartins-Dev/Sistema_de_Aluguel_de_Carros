
# Etapa 1: Compilação
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

COPY src ./src

RUN ./mvnw clean package -DskipTests

# Etapa 2: Execução
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/aluguelcarros-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
