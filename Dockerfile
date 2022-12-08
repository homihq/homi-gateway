FROM eclipse-temurin:17-jdk-alpine

COPY target/homi-gateway.jar homi-gateway.jar

ENTRYPOINT ["java","-jar","homi-micro.jar"]
