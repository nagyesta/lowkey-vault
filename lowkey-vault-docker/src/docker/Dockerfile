#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:e914d4e97cc467f99befdc751e1fa1b6c9ec77fc9396caae2e37c48c9e44a681
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
