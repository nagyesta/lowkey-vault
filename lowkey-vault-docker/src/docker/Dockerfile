#openjdk:11.0.15-jre-slim-buster amd64
FROM openjdk@sha256:dd2dd82151b2d1761f3744b039365140b882335b529199b765a3993cca076f71
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
