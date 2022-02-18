#openjdk:11.0.14-jre-slim-buster amd64
FROM openjdk@sha256:38c34fcaf760720d96f74a4b6793da532db1e464ccd51731de8407ddf4bbe197
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
