#openjdk:11.0.14.1-jre-slim-buster amd64
FROM openjdk@sha256:f2de674baa154219b8cf4678f941e714fcdb98904ca2bd975cac3523cfecc787
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
