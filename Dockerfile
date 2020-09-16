FROM adoptopenjdk/openjdk11:alpine-jre
#FROM openjdk:11

ARG JAR_FILE=./wnd-spring-boot-template-provider/target/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=${JAVA_OPTS:-""}
CMD java ${JAVA_OPTS} -jar /app.jar