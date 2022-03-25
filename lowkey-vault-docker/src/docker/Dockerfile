#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:d3ba674d6cbaf782030d708c3bd5eec291f56de2473a6136b9ab427753810482
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
