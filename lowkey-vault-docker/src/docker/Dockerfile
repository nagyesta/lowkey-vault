FROM eclipse-temurin:11.0.16_8-jre-alpine@sha256:8b432d7aed1ae6d87a568842a04e4a7bfc11b880ce7b1a98cb9a252d864ead93
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8443:8443
ADD lowkey-vault.jar /lowkey-vault.jar
RUN mkdir "/import"
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
