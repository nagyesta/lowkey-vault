FROM --platform=${BUILDPLATFORM} eclipse-temurin:25.0.1_8-jre-alpine-3.22@sha256:9c65fe190cb22ba92f50b8d29a749d0f1cfb2437e09fe5fbf9ff927c45fc6e99
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
