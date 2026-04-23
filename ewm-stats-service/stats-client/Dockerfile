FROM eclipse-temurin:21-jre-jammy

WORKDIR /client

COPY target/stats-client-0.0.1-SNAPSHOT.jar client.jar

EXPOSE 8080

CMD ["java", "-jar", "client.jar"]