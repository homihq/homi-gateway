FROM eclipse-temurin:17-jdk-alpine

COPY homi-micro.jar homi-micro.jar

ENTRYPOINT ["java","-jar","homi-micro.jar"]
