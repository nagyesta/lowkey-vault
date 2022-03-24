#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:198414c99a9d3011d65b22d5ce75af4b54c72d4fcdbf62fa3058434846c9f201
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
