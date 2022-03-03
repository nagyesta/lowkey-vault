#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:d9128fbdb150e4631dc75940c4227cb822ea6b77b9ce2279c9bbff54a4a9c044
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
