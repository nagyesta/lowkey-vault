#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:69171da5aa2d4930cd4f58701612aa33a684b8329c01bfd7d382277a44ddc21f
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
