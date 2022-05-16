FROM eclipse-temurin:11.0.15_10-jre-alpine@sha256:44984e6bd1a3d394494ccfe8347f09595c2410fd4b345373bbd4b3b69d8cac50
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
