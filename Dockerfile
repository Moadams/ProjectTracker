FROM eclipse-temurin:24-jdk-noble AS builder

RUN apt-get update && \
    apt-get install -yq maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

RUN mvn clean install -DskipTests

RUN ls -l /app/target/

FROM eclipse-temurin:24-jre-noble


WORKDIR /app


COPY --from=builder /app/target/*.jar app.jar


EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]
