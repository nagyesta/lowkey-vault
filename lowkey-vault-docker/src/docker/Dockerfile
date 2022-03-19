#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:004d70432d691f30b05efac5623c29d264a0f6612500ddd859299d4ed7e66ea5
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
