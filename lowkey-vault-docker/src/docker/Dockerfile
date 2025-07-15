FROM --platform=${BUILDPLATFORM} eclipse-temurin:17.0.15_6-jre-alpine@sha256:2f5f20b9dd8f70c30ed1eba9fdf362d2abf4eac45376a29fb7c8a5e919d0da68
LABEL maintainer="nagyesta@gmail.com"
EXPOSE 8080 8443
COPY lowkey-vault.jar /lowkey-vault.jar
RUN \
  chmod 555 "/lowkey-vault.jar" && \
  mkdir "/import" && \
  chmod 755 "/import" && \
  mkdir "/config" && \
  chmod 555 "/config"
WORKDIR /
CMD [ "sh", "-c", "ls /" ]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /lowkey-vault.jar ${LOWKEY_ARGS}"]
