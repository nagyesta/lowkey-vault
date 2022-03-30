#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:923f66069a83b289d873b26248e7978b223ba23b4c9ab7611c69cb4622560fc6
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
