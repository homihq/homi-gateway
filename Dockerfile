FROM eclipse-temurin:17-jdk-alpine

COPY /home/runner/work/homi-gateway/homi-gateway/target/homi-micro.jar homi-micro.jar

ENTRYPOINT ["java","-jar","homi-micro.jar"]
