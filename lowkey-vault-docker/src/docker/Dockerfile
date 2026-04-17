FROM --platform=${BUILDPLATFORM} eclipse-temurin:25.0.2_10-jre-alpine-3.22@sha256:bd485189b0a07eeab762f931b48ec39853b4977853d26fe73fbea0b7218d1528
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
