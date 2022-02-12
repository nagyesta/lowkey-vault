#openjdk:11.0.14-jre-slim-buster amd64
FROM openjdk@sha256:98f19a6e8f22ab752fe98c2c5e4c100d2f5a32eedfd4ac5b8adb9bd3a390c53e
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
