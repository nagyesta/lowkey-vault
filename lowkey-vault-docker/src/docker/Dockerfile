#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:3d88de143bdd54d3e873302355564818e0ac0d17dbacc9869090197b7dd8a8f3
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
