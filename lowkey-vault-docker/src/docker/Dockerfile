FROM eclipse-temurin:17.0.4_8-jre-alpine@sha256:a65667aa49d8e8b9094677f5f9c8dca13cef176ac5069dfb31b0e5fe92d08a5d
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
