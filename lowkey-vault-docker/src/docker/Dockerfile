#openjdk:11.0.14-jre-slim-buster amd64
FROM openjdk@sha256:2c3ffd17a95947a187cbf00b454656566063cc53f21ea419929c0d23f3347572
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
