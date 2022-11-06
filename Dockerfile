FROM eclipse-temurin:17-jdk-alpine

COPY target/homi-micro.jar homi-micro.jar

ENTRYPOINT ["java","-jar","homi-micro.jar"]
