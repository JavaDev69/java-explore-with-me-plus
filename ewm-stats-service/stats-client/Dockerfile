FROM eclipse-temurin:21-jre-jammy

WORKDIR /client

COPY target/stats-client-0.0.1-SNAPSHOT.jar client.jar

EXPOSE 9090

CMD ["java", "-jar", "client.jar"]